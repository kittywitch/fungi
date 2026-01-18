(ns fungi.aside
  (:require [clojure.pprint :as pprint]
            [clojure.zip :as zip]
            [fungi.hickory :as fh]
            [hickory.core :as hc]
            [hickory.select :as hs]))

(def aside-selector
  (hs/and (hs/tag :aside)
          (hs/not (hs/ancestor (hs/tag :aside) (hs/and (hs/tag :hr)
                                     (hs/id "note"))))))
(def note-rule {:type :element
            :tag :hr
            :attrs {:id "note"}
            :content nil})

(defn aside-editor [elem]
  (let [{content :content} elem
        new-content (concat [note-rule] content [note-rule])]
    (assoc elem :content new-content)))

(defn aside-noter [tree]
  (fh/hickory-update
   aside-selector
   tree
   #(zip/edit % aside-editor)))
