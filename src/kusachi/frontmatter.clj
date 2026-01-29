(ns kusachi.frontmatter
  (:require [cheshire.core :as ch]
            [clojure.pprint :as pprint]
            [clojure.zip :as zip]
            [kusachi.hickory :as fh]
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

(defn kusachi-selector []
  (hs/tag :kusachi))

(defn kusachi-cleanup-selector []
  (hs/tag :kusachi-remove))

(defn fallback-func [elem content]
  (assoc elem :tag :span :content [content]))

(defn kusachi-title [elem content]
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
               :content [(hickory-link (str "/tags/" el ".html") (str el))]})})

(defn kusachi-tags [elem content]
  (assoc elem
         :tag :dt
         :content [(tag-list content)]))

(defn kusachi-draft [elem content]
  (if content
    (assoc elem :tag :i :content ["This page is a draft!"])
    (assoc elem :tag :kusachi-remove)))

(defn kusachi-date [elem content]
  (assoc elem
         :tag :time
         :attrs {:datetime content}
         :content [content]))

(defn kusachi-embedder [elem frontmatter]
  (let [{{id :id} :attrs} elem
        {content id} frontmatter
        {func (keyword id)} {:date kusachi-date
                             :title kusachi-title
                             :draft kusachi-draft
                             :tags kusachi-tags}]
    (if content
      (if func (func elem content) (fallback-func elem content))
      (assoc elem :tag :kusachi-remove))))

(defn kusachi-title-setter [elem frontmatter]
  (let [{title "title"} frontmatter]
    (if title
      (assoc elem :content [title " - dork.dev"] :attrs {})
      elem)))

(defn kusachi-og-setter [elem frontmatter]
  (let [{title "title"} frontmatter]
    (if title
      (update-in (assoc-in elem [:attrs :content] title) [:attrs] dissoc :id)
      elem)))

(defn kusachi-replacer [frontmatter tree]
  (let [embedded (fh/hickory-update
                  (kusachi-selector)
                  tree
                  #(zip/edit % kusachi-embedder frontmatter))
        cleaned (fh/hickory-update
                 (kusachi-cleanup-selector)
                 embedded
                 zip/remove)
        with-title (fh/hickory-update
                    (hs/and (hs/tag :title)
                            (hs/id :placeholder))
                    cleaned
                    #(zip/edit % kusachi-title-setter frontmatter))
        with-og (fh/hickory-update
                  (hs/and (hs/tag :meta)
                          (hs/id :og-title-placeholder))
                  with-title
                  #(zip/edit % kusachi-og-setter frontmatter))
                    ] with-og))
