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
            [kusachi.posts :as fpo]
            [kusachi.sass :as fsa]
            [kusachi.thumbnailing :as ft]
            [kusachi.routing :as fr]
            [kusachi.server :as fs]
            [hickory.core :as hic]
            [hickory.render :refer [hickory-to-html]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def img-core {:path "resources/img"
                :lens {:filter [(fc/glob "*.{svg,png,gif,jpg,webp}")]
                       :remove []}
                :router [fr/resource-router]
                :compiler (fn [path out-path] (fc/copy-file-compiler path out-path))})
(def js-core {:path "resources/js"
                :lens {:filter [(fc/glob "*.js")]
                       :remove []}
                :router [fr/resource-router]
                :compiler (fn [path out-path] (fc/copy-file-compiler path out-path))})

(defn generate-tag [tag paths]
  (let [tag-post-list (map (fn [path]
           (assoc (get @fpo/post-map path)
                  "path" path)
           ) paths)]
    (fco/postlist-for-tag tag tag-post-list)))

(defn generate-tags []
  (println "Generating tag pages")
  (doseq [[tag paths]
          @fpo/tag-map]
    (println (str "Generating page for tag " tag))
    (let [taglist (generate-tag tag paths)
      file (fc/simple-writer (str "output/tags/" tag ".html") taglist)]
      file )))

(defn generate-index []
  (println "Generating index page")
  (let [posts (fco/postlist @fpo/post-map @fpo/tag-map)
        file (fc/simple-writer "output/index.html" posts)]
    file))

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
  (fc/load-output-hashset)
  (println "Starting operation")
  (let [cores [fpo/post-core fsa/sass-core ft/image-core ft/thumb-core fhi/arborium-theme-core img-core js-core]]
    (mapv fc/pipeline cores))
  (generate-tags)
  (generate-index)
  (println "Finished operation")
  (fc/commit-output-hashset)
  (println "Post map")
  (pprint/pprint @fpo/post-map)
  (println "Tag map")
  (pprint/pprint @fpo/tag-map)
  (println "Image map")
  (pprint/pprint @ft/img-map)
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
