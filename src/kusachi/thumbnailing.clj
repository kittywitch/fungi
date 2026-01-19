(ns kusachi.thumbnailing
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [clojure.zip :as zip]
            [fivetonine.collage.core :as fcc]
            [fivetonine.collage.util :as fcu]
            [pathetic.core :as pc]
            [kusachi.hickory :as fh]
            [kusachi.core :as fc]
            [kusachi.routing :as fr]
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
  (hs/and (hs/descendant (hs/tag :article) (hs/tag :img))
          (hs/not (hs/child (hs/tag :a) (hs/tag :img)))))

(defn image-template [elem image]
  (let [
        new-img (assoc-in elem [:attrs :src] (add-thumb-to-filename image))
        img-wrapped {:type :element
                     :tag :a
                     :attrs {:href image}
                     :content [
                               new-img
                               ]}

        ] img-wrapped))

(defn image-editor [elem]
  (let [{attrs :attrs} elem
        {src :src} attrs
      ] (image-template elem src)))

(defn image-thumber [post-path images tree]
  (fh/hickory-update
   (image-selector post-path images)
   tree
   #(zip/edit % image-editor)))

(def img-map (atom {}))
(defn create-thumbnail
  [path out-path]
  (swap! img-map assoc out-path path)
  (let [image (fcu/load-image path)
        resized (fcc/resize image :width 600)
        parent (.getParentFile (io/as-file out-path))]
    (.mkdirs parent)
    (fcu/save resized out-path :quality 0.7 :progressive true)))

(defn thumb-exists [file]
  (let [thumb-filename (add-thumb-to-filename (.getAbsolutePath file))]
    (.exists (io/as-file thumb-filename))))

(def image-core {:path "posts"
                 :lens {:filter [(fc/glob "*.{png,jpg,webp,gif,bmp}")]
                        :remove [(fc/glob "*.thumb.*")]}
                 :router [fr/output-router]
                 :compiler (fn [path out-path] (fc/copy-file-compiler path out-path))})

(def thumb-core {:path "posts"
                 :lens {:filter [(fc/glob "*.{png,jpg,webp,gif,bmp}")]
                        :remove [(fc/glob "*.thumb.*") thumb-exists]}
                 :router [fr/output-router add-thumb-to-filename]
                 :compiler (fn [path out-path] (create-thumbnail path out-path))})
