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
