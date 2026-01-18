(ns fungi.aside
  (:require [clojure.pprint :as pprint]
            [clojure.zip :as zip]
            [fungi.hickory :as fh]
            [hickory.core :as hc]
            [hickory.select :as hs]))

(def all-headings
  (hs/or (hs/tag :h1)
                 (hs/tag :h2)
                 (hs/tag :h3)
                 (hs/tag :h4)
                 (hs/tag :h5)
                 (hs/tag :h6)))
(def heading-selector
  ; the first criteria here, for headings not in a block quote, oh god
  (hs/and (hs/not (hs/or (hs/descendant (hs/tag :blockquote) all-headings)
                         (hs/class "linked")
                         (hs/id "toc-title")
                         (hs/id "title")))
          all-headings))

(defn heading-editor [elem]
  (let [{heading-attrs :attrs} elem
        {heading-id :id} heading-attrs
        heading-link-item {:type :element
                           :tag :a
                           :attrs {:href (str "#" heading-id)}
                           :content ["§"]}
        {heading-content :content} elem]
    (fh/deep-merge elem {:content (into [heading-link-item " "] heading-content)
                         :attrs {:class "linked"}})))

(defn heading-linker [tree]
  (fh/hickory-update
   heading-selector
   tree
   #(zip/edit % heading-editor)))
