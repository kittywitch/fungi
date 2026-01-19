(ns kusachi.main
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [fivetonine.collage.core :as fcc]
            [fivetonine.collage.util :as fcu]
            [kusachi.components :as fco]
            [kusachi.core :as fc]
            [kusachi.frontmatter :as fm]
            [kusachi.headings :as fhe]
            [kusachi.highlighting :as fhi]
            [kusachi.aside :as fas]
            [kusachi.preprocessor :as fp]
            [kusachi.thumbnailing :as ft]
            [kusachi.routing :as fr]
            [kusachi.server :as fs]
            [hickory.core :as hic]
            [hickory.render :refer [hickory-to-html]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn simple-writer [out-path data]
  (let [parent (.getParentFile (io/as-file out-path))]
    (.mkdirs parent))
  (with-open [file-writer (io/writer out-path :append false)]
    (.write file-writer data)))

(defn add-thumb-to-filename [filename]
  (str/replace filename #"(\.[a-zA-Z0-9]+)$" ".thumb$1"))

(defn thumb-exists [file]
  (let [thumb-filename (add-thumb-to-filename (.getAbsolutePath file))]
    (.exists (io/as-file thumb-filename))))

(def img-map (atom {}))

(defn create-thumbnail
  [path out-path]
  (swap! img-map assoc out-path path)
  (let [image (fcu/load-image path)
        resized (fcc/resize image :width 600)
        parent (.getParentFile (io/as-file out-path))]
    (.mkdirs parent)
    (fcu/save resized out-path :quality 0.7 :progressive true)))

(defn glob-criteria [criteria]
  (str "glob:" criteria))

(defn glob-matcher [criteria]
  (.getPathMatcher
   (java.nio.file.FileSystems/getDefault)
   (glob-criteria criteria)))

(defn copy-file-compiler [path out-path]
  (let [file (io/as-file path)
        out-file (io/as-file out-path)
        parent (.getParentFile out-file)]
    (.mkdirs parent)
    (io/copy file out-file)))

(defn glob [criteria]
  (fn [path] (.matches (glob-matcher criteria) (.getFileName (.toPath path)))))

(def post-map (atom {}))
(def tag-map (atom {}))

(defn assign-post-to-tags [path frontmatter]
  (let [{tags "tags"} frontmatter]
    (doseq [tag tags]
        (let [
            {tag-data tag :or {tag-data []}} @tag-map
        ]
        (swap! tag-map assoc tag (conj tag-data path)))
      )))

(def post-core {:path "posts"
                :lens {:filter [(glob "*.md")]
                       :remove []}
                :recalculate (fn [] true)
                :router [fr/output-router (fr/refiletyper "md" "html")]
                :compiler (fn [path out-path]
                            (->> path
                                 (fp/pandoc "markdown" "html" true)
                                 (#(fco/blogpost :content [:hiccup/raw-html %]
                                                 :title "placeholder"))
                                 (#(hic/as-hickory (hic/parse %)))
                                 ; This let may as well be considered "templateable post context".
                                 (#(let [{:keys [frontmatter cleantree]} (fm/frontmatter-extractor %)
                                         current-path (.getAbsolutePath (io/file "./"))
                                         relative-out (fr/relative-path current-path out-path)]
                                     (swap! post-map assoc relative-out frontmatter)
                                     (assign-post-to-tags relative-out frontmatter)
                                     (let [
                                          fm-replaced (fm/kusachi-replacer frontmatter cleantree)
                                          thumby (ft/image-thumber relative-out img-map fm-replaced)
                                          ] thumby)))
                                 (fhi/code-replacer)
                                 (fas/aside-noter)
                                 (fhe/heading-linker)
                                 (hickory-to-html)
                                 (simple-writer out-path)))})

(defn resource-router [filename]
  (str/replace filename "resources/" "output/assets/"))

; Resources (images, scss) for the website

(def logo-core {:path "resources/img"
                :lens {:filter [(glob "*.svg")]
                       :remove []}
                :router [resource-router]
                :compiler (fn [path out-path] (copy-file-compiler path out-path))})

(defn scss-router [filename]
  (str/replace filename "scss/" "css/"))

(def sass-core {:path "resources/scss/"
                :lens {:filter [(glob "*.scss")]
                       :remove []}
                :router [resource-router scss-router (fr/refiletyper "scss" "css")]
                :compiler (fn [path out-path] (fp/sass path out-path))})

; Images from posts

(def image-core {:path "posts"
                 :lens {:filter [(glob "*.{png,jpg,webp,gif,bmp}")]
                        :remove [(glob "*.thumb.*")]}
                 :router [fr/output-router]
                 :compiler (fn [path out-path] (copy-file-compiler path out-path))})

(def thumb-core {:path "posts"
                 :lens {:filter [(glob "*.{png,jpg,webp,gif,bmp}")]
                        :remove [(glob "*.thumb.*") thumb-exists]}
                 :router [fr/output-router add-thumb-to-filename]
                 :compiler (fn [path out-path] (create-thumbnail path out-path))})

(defn generate-tag [tag paths]
  (let [tag-post-list (map (fn [path]
           (assoc (get @post-map path)
                  "path" path)
           ) paths)]
    (fco/postlist-for-tag tag tag-post-list)))

(defn generate-tags []
  (println "Generating tag pages")
  (doseq [[tag paths]
          @tag-map]
    (println (str "Generating page for tag " tag))
    (let [taglist (generate-tag tag paths)
      file (simple-writer (str "output/tags/" tag ".html") taglist)]
      file )))

(defn generate-index []
  (println "Generating index page")
  (let [posts (fco/postlist @post-map @tag-map)
        file (simple-writer "output/index.html" posts)]
    file))

(def sha-map (atom {}))

(def cli-options
  [ ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["kusachi, a clojure static site generator"
        ""
        "Usage: kusachi [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  generate Generate the website"
        "  serve    Serve the website"]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"generate" "serve"} (first arguments)))
      {:action (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn generate []
  (pprint/pprint (meta #'fco/page-raw))
  (println "Loading prior output hashset")
  (fc/load-output-hashset sha-map)
  (println "Starting operation")
  (let [cores [post-core sass-core image-core logo-core thumb-core]]
    (mapv (partial fc/pipeline sha-map) cores))
  (generate-tags)
  (generate-index)
  (println "Finished operation")
  (fc/commit-output-hashset sha-map)
  (println "Post map")
  (pprint/pprint @post-map)
  (println "Tag map")
  (pprint/pprint @tag-map)
  (println "Image map")
  (pprint/pprint @img-map)
  (shutdown-agents))

(defn -main [& args]
  (pprint/pprint args)
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "generate"  (generate)
        "serve" (fs/serve)
      ))))
