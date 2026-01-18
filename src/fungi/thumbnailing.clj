(ns fungi.thumbnailing
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str]
            [clojure.zip :as zip]
            [pathetic.core :as pc]
            [fungi.hickory :as fh]
            [hickory.core :as hc]
            [hickory.select :as hs]
            [lambdaisland.uri :as uri]))

(defn check-external [url]
  (println (str "checking url: " url))
  (not (uri/relative? url)))

(defn exists-in-images [image-url post-path images]
  (println "checking exists")
  (let [
      basedir (pc/up-dir post-path)
      adjusted (pc/resolve basedir image-url)
      within (contains? images adjusted)
  ] (pprint/pprint {:basedir basedir
                    :adjusted adjusted
                    :within within})
    within))

(defn add-thumb-to-filename [filename]
  (str/replace filename #"(\.[a-zA-Z0-9]+)$" ".thumb$1"))

(defn image-selector [post-path images]
  (println "selector called")
  (hs/and (hs/descendant (hs/tag :article) (hs/tag :img))
          (hs/not (hs/child (hs/tag :a) (hs/tag :img)))))
    ;(hs/descendant (hs/tag :article) (hs/not (hs/tag :a)) (hs/tag :img))))
                                                  ;(hs/attr :href #(exists-in-images % post-path images))
                                                  ;(hs/attr :href #(check-external %)))))

(defn image-template [elem image]
  (let [
        new-img (assoc-in elem [:attrs :src] (add-thumb-to-filename image))
        img-wrapped {:type :element
                     :tag :a
                     :attrs {:href image}
                     :content [
                                new-img
                                ]}

        ]
      img-wrapped
    )
  )

(defn image-editor [elem]
  (println elem)
  (let [{attrs :attrs} elem
        {src :src} attrs
      ] (image-template elem src)))

(defn image-thumber [post-path images tree]
  (fh/hickory-update
   (image-selector post-path images)
   tree
   #(zip/edit % image-editor)))
