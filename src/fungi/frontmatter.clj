(ns fungi.frontmatter
  (:require [cheshire.core :as ch]
            [clojure.pprint :as pprint]
            [clojure.zip :as zip]
            [fungi.hickory :as fh]
            [hickory.select :as hs]))

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

(defn frontmatter-extractor [tree]
  (let [frontmatter-elem (hs/select (frontmatter-selector) tree)
        frontmatter (frontmatter-decode frontmatter-elem)
        clean-tree (fh/hickory-update
                    (frontmatter-selector)
                    tree
                    zip/remove)]
    {:cleantree clean-tree
     :frontmatter frontmatter}))

(defn fungi-selector []
  (hs/tag :fungi))

(defn fungi-cleanup-selector []
  (hs/tag :fungi-remove))

(defn fallback-func [elem content]
  (assoc elem :tag :span :content [content]))

(defn fungi-title [elem content]
  (assoc elem :tag :h1 :content [content]))

(defn hickory-link [destination text]
  {:type :element
   :tag :a
   :attrs {:href destination}
   :content [text]})

(defn tag-list [lst]
  {:type :element
   :tag :ul
   :attrs {:class "inline-list" :id "tags"}
   :content (for [el lst]
              {:type :element
               :tag :li
               :attrs nil
               :content [(hickory-link (str "/tags/" el) (str el))]})})

(defn fungi-tags [elem content]
  (assoc elem
         :tag :dt
         :content [(tag-list content)]))

(defn fungi-draft [elem content]
  (if content
    (assoc elem :tag :i :content ["This page is a draft!"])
    (assoc elem :tag :fungi-remove)))

(defn fungi-date [elem content]
  (assoc elem
         :tag :time
         :attrs {:datetime content}
         :content [content]))

(defn fungi-embedder [elem frontmatter]
  (let [{{id :id} :attrs} elem
        {content id} frontmatter
        {func (keyword id)} {:date fungi-date
                             :title fungi-title
                             :draft fungi-draft
                             :tags fungi-tags}]
    (if content
      (if func (func elem content) (fallback-func elem content))
      (assoc elem :tag :fungi-remove))))

(defn fungi-title-setter [elem frontmatter]
  (let [{title "title"} frontmatter]
    (if title
      (assoc elem :content [title " - dork.dev"] :attrs {})
      elem)))

(defn fungi-replacer [frontmatter tree]
  (let [embedded (fh/hickory-update
                  (fungi-selector)
                  tree
                  #(zip/edit % fungi-embedder frontmatter))
        cleaned (fh/hickory-update
                 (fungi-cleanup-selector)
                 embedded
                 zip/remove)
        with-title (fh/hickory-update
                    (hs/and (hs/tag :title)
                            (hs/id :placeholder))
                    cleaned
                    #(zip/edit % fungi-title-setter frontmatter))] with-title))
