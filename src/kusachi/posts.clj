(ns kusachi.posts
  (:require [clojure.java.io :as io]
            [kusachi.components :as fco]
            [kusachi.core :as fc]
            [kusachi.frontmatter :as fm]
            [kusachi.headings :as fhe]
            [kusachi.highlighting :as fhi]
            [kusachi.aside :as fas]
            [kusachi.preprocessor :as fp]
            [kusachi.thumbnailing :as ft]
            [kusachi.routing :as fr]
            [hickory.core :as hic]
            [hickory.render :refer [hickory-to-html]]))

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
                :lens {:filter [(fc/glob "*.md")]
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
                                          thumby (ft/image-thumber relative-out ft/img-map fm-replaced)
                                          ] thumby)))
                                 (fhi/code-replacer)
                                 (fas/aside-noter)
                                 (fhe/heading-linker)
                                 (hickory-to-html)
                                 (fc/simple-writer out-path)))})


