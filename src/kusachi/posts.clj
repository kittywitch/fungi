(ns kusachi.posts
  (:require [clojure.java.io :as io]
            [clojure.zip :as zip]
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
            [kusachi.hickory :as kh]
            [hickory.select :as hs]
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

(def excess-selector
  (hs/or (hs/and (hs/tag :nav)
                 (hs/id "TOC"))
         (hs/tag "kusachi")
         (hs/tag :hr)
         (hs/and (hs/tag :dl))
                  (hs/id "frontmatter")))

(defn prepare-xml-post [tree]
  (kh/hickory-update
    excess-selector
    tree
    zip/remove))

(defn xml-fragment-wrapper [elems]
  {:type :element
   :tag :article
   :attrs nil
   :content elems})

(def post-core {:path "posts"
                :lens {:filter [(fc/glob "*.md")]
                       :remove []}
                :recalculate (fn [] true)
                :router [fr/output-router (fr/refiletyper "md" "html")]
                :compiler (fn [path out-path]
                            (->> path
                                 (#(let [
                                         post-html(fp/pandoc "markdown" "html" true %)
                                         templated (fco/blogpost :content [:hiccup/raw-html post-html]
                                                                   :title "placeholder")
                                         hickory-templated (hic/as-hickory (hic/parse templated))
                                         ; This let may as well be considered "templateable post context".
                                         {:keys [frontmatter cleantree]} (fm/frontmatter-extractor hickory-templated)
                                               current-path (.getAbsolutePath (io/file "./"))
                                               output-path (.getAbsolutePath (io/file "./output"))
                                               relative-out (fr/relative-path current-path out-path)
                                               relative-output (fr/relative-path output-path out-path)
                                               rsstree (prepare-xml-post (xml-fragment-wrapper (map hic/as-hickory (hic/parse-fragment post-html))))
                                               rsshtml (hickory-to-html rsstree)
                                               context (assoc frontmatter :rss rsshtml :rel relative-output)]
                                     (swap! post-map assoc relative-out context)
                                     (assign-post-to-tags relative-out context)
                                     (let [
                                          fm-replaced (fm/kusachi-replacer context cleantree)
                                          thumby (ft/image-thumber relative-out ft/img-map fm-replaced)
                                          ] thumby)))
                                 (fhi/code-replacer)
                                 (fas/aside-noter)
                                 (fhe/heading-linker)
                                 (hickory-to-html)
                                 (fc/simple-writer out-path)))})


