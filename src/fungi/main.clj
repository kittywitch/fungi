(ns fungi.main
  (:require [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [clojure.zip :as zip]
            [hickory.zip :as hz]
            [hickory.core :as hic]
            [hickory.select :as hs]
            [hickory.render :refer [hickory-to-html]]
            [fivetonine.collage.util :as fcu]
            [fivetonine.collage.core :as fcc]
            [fungi.components :as fco]
            [fungi.core :as fc]
            [fungi.preprocessor :as fp]
            [fungi.routing :as fr]
            [cheshire.core :as ch]))


(defn sass [in out]
  (shell/sh "sass" (str in ":" out)))

; https://github.com/clj-commons/hickory/issues/41#issuecomment-383893434
(defn hickory-update [selector-fn hickory-tree zip-fn]
  (loop [zip (hz/hickory-zip hickory-tree)
         next (hs/select-next-loc selector-fn zip)]
    (if next
      (let [new-zip (zip-fn next)]
        (recur new-zip (hs/select-next-loc selector-fn new-zip)))
      (zip/root zip))))

; https://clojuredocs.org/clojure.core/merge#example-5b80849ee4b00ac801ed9e75
; Currently unused, but very useful when merging hickory trees
(defn deep-merge [v & vs]
  (letfn [(rec-merge [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (if (some identity vs)
      (reduce #(rec-merge %1 %2) v vs)
      (last vs))))

(defn frontmatter-selector []
  (hs/child (hs/and (hs/tag :data)
                    (hs/id :frontmatter))))

(defn frontmatter-decode [elem]
  (let [[elem-delisted] elem
        {:keys [content]} elem-delisted
        [content-delisted] content
        data (ch/parse-string content-delisted)]
    (print "Frontmatter: ")
    (pprint/pprint data)
    (identity data)))

(defn generalized-frontmatter-extractor [tree]
  (let [frontmatter-elem (hs/select (frontmatter-selector) tree)
        frontmatter (frontmatter-decode frontmatter-elem)
        clean-tree (hickory-update
                     (frontmatter-selector)
                     tree
                     zip/remove)]
    {:cleantree clean-tree
     :frontmatter frontmatter}
    ))

(defn fungi-selector []
  (hs/tag :fungi))

(defn fungi-cleanup-selector []
  (hs/tag :fungi-remove))

(defn fungi-title [elem content]
  (assoc elem :tag :h1 :content [content]))

(defn fungi-date [elem content]
  (assoc elem
         :tag :time
         :attrs {:datetime content}
         :content [content]))

(defn fungi-embedder [elem frontmatter]
  (let [{{id :id} :attrs} elem
        {content id} frontmatter
        {func (keyword id)} {:date fungi-date
            :title fungi-title}]
  (if content
    (func elem content)
    (assoc elem :tag :fungi-remove))))

(defn fungi-title-setter [elem frontmatter]
  (let [{title "title"} frontmatter]
    (if title
      (assoc elem :content [title " - dork.dev"] :attrs {})
      elem)))

(defn fungi-replacer [frontmatter tree]
  (let [embedded (hickory-update
                   (fungi-selector)
                   tree
                   #(zip/edit % fungi-embedder frontmatter))
        cleaned (hickory-update
                  (fungi-cleanup-selector)
                  embedded
                  zip/remove
                  )
        with-title (hickory-update
                     (hs/and (hs/tag :title)
                             (hs/id :placeholder))
                     cleaned
                     #(zip/edit % fungi-title-setter frontmatter))] with-title))


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

(defn create-thumbnail
  [path out-path]
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
                                 (#(let [{:keys [frontmatter cleantree]} (generalized-frontmatter-extractor %)
                                         current-path (.getAbsolutePath (io/file "./"))
                                         relative-out (fr/relative-path current-path out-path)
                                         ]
                                     (swap! post-map assoc relative-out frontmatter)
                                     (fungi-replacer frontmatter cleantree)))
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
                :compiler (fn [path out-path] (sass path out-path))})

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

(defn generate-index []
  (println "Generating index page")
  (let [posts (fco/postlist @post-map)
        file (simple-writer "output/index.html" posts)]
    file))


(def sha-map (atom {}))

(defn -main
  []
  (pprint/pprint (meta #'fco/page-raw))
  (println "Loading prior output hashset")
  (fc/load-output-hashset sha-map)
  (println "Starting operation")
  (let [cores [post-core sass-core image-core logo-core thumb-core]]
    (mapv (partial fc/pipeline sha-map) cores))
  (generate-index)
  (println "Finished operation")
  (fc/commit-output-hashset sha-map)
  (println "Post map")
  (pprint/pprint @post-map)
  (shutdown-agents))
