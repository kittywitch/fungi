(ns kusachi.routing
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

; https://stackoverflow.com/questions/26170493/function-that-gives-the-relative-path-in-clojure
(defn relative-path
  [a b]
  (let [path-a (.toPath (io/file a))
        path-b (.toPath (io/file b))
        can-relativize? (if (.getRoot path-a)
                          (some? (.getRoot path-b))
                          (not (.getRoot path-b)))]
    (when can-relativize?
      (str (.relativize path-a path-b)))))

(defn output-router [filename]
  (str/replace filename "posts" "output/posts"))

(defn resource-router [filename]
  (str/replace filename "resources/" "output/assets/"))

(defn root-resource-router [filename]
  (str/replace filename "resources/" "output/"))

(defn refiletyper [from to]
  (fn [file] (str/replace file (re-pattern (str "(\\." from ")$")) (str "." to))))
