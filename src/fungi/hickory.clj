(ns fungi.hickory
  (:require [clojure.zip :as zip]
            [hickory.select :as hs]
            [hickory.zip :as hz]))

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
