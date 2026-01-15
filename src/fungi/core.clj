(ns fungi.core
  (:require [clj-commons.digest :as digest]
            [fungi.routing :as fr]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint]))



(defn pipe [initial-data my-functions] ((apply comp my-functions) initial-data))

(defn pipeline-hash [path]
  (let [path-digest (digest/sha-256 (io/file path))]
    path-digest))

(defn pipeline-file [sha-map router compiler path]
  ; TODO: make this less inefficient?
  (let [out-path (pipe path router)
        current-path (.getAbsolutePath (io/file "./"))
        relative-current (fr/relative-path current-path path)
        relative-out (fr/relative-path current-path out-path)
        path-hash (pipeline-hash path)]
    (println (str "← " relative-current))
    (println (str "→ " relative-out))
    (swap! sha-map assoc relative-current path-hash)
    (println (str "# " path-hash))
    (let [result (compiler path out-path)]
      (println)
      (identity result))))

(defn pipeline
  [sha-map core]
  (let [{:keys [path compiler router]
         {filters :filter
          removes :remove} :lens
         :or {
              filters []
              removes []
              compiler (fn [])
              router [fr/output-router]
          }
         } core
        filterer (when (> (count filters) 0) (apply every-pred filters))
        remover (when (> (count removes) 0) (apply some-fn removes))]
    (->> path
         io/file
         file-seq
         (filter #(.isFile %))
         (#(if (> (count filters) 0) (filter filterer %) %))
         (#(if (> (count removes) 0) (remove remover %) %))
         (mapv #(.getAbsolutePath %))
         (sort)
         (reverse)
         (mapv (partial pipeline-file sha-map router compiler))
      )
    ))


(defn load-output-hashset [sha-map]
  (let [file-content (with-open [rdr (io/reader "./fungi.lock")]
                       (edn/read (new java.io.PushbackReader rdr)))]
    (reset! sha-map file-content)
    (println "Loaded prior output hashset")
    (print "Contents: ")
    (pprint/pprint @sha-map)))


(defn commit-output-hashset [sha-map]
  (println "Committing output hashset")
  (with-open [w (io/writer "./fungi.lock" :append false)]
    (.write w (prn-str @sha-map))))
