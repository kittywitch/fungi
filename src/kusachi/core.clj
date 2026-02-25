(ns kusachi.core
  (:require [clj-commons.digest :as digest]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [kusachi.routing :as fr]))

(def sha-map (atom {}))

(defn simple-writer [out-path data]
  (let [parent (.getParentFile (io/as-file out-path))]
    (.mkdirs parent))
  (with-open [file-writer (io/writer out-path :append false)]
    (.write file-writer data)))

(defn copy-file-compiler [path out-path]
  (let [file (io/as-file path)
        out-file (io/as-file out-path)
        parent (.getParentFile out-file)]
    (.mkdirs parent)
    (io/copy file out-file)))

(defn glob-criteria [criteria]
  (str "glob:" criteria))

(defn glob-matcher [criteria]
  (.getPathMatcher
   (java.nio.file.FileSystems/getDefault)
   (glob-criteria criteria)))

(defn glob [criteria]
  (fn [path] (.matches (glob-matcher criteria) (.getFileName (.toPath path)))))

(defn pipe [initial-data my-functions] ((apply comp my-functions) initial-data))

(defn pipeline-hash [path]
  (let [path-digest (digest/sha-256 (io/file path))]
    path-digest))

(defn pipeline-file [recalculate router compiler path]
  ; TODO: make this less inefficient?
  (let [out-path (pipe path router)
        current-path (.getAbsolutePath (io/file "./"))
        relative-current (fr/relative-path current-path path)
        relative-out (fr/relative-path current-path out-path)
        path-hash (pipeline-hash path)
        existing-hash (get @sha-map relative-current)
        file-exists (.exists (io/as-file relative-out))]
    (println (str "← " relative-current))
    (println (str "→ " relative-out))
    (swap! sha-map assoc relative-current path-hash)
    (println (str "# " path-hash))
    (if (or (recalculate) (not file-exists) (not= path-hash existing-hash)) (let [result (compiler path out-path)]
      (println)
      (identity result)) (println (str "Skipping compilation of " path ", hashes are equal, destination exists and recalculate function returns false.")))))

(defn pipeline
  [core]
  (let [{:keys [path compiler router recalculate]
         {filters :filter
          removes :remove} :lens
         :or {filters []
              recalculate (fn [] false)
              removes []
              compiler (fn [])
              router [fr/output-router]}} core
        filterer (when (> (count filters) 0) (apply every-pred filters))
        remover (when (> (count removes) 0) (apply some-fn removes))]
    (->> path
         io/file
         file-seq
         (filter #(.isFile %))
         (#(if (> (count filters) 0) (filter filterer %) %))
         (#(if (> (count removes) 0) (remove remover %) %))
         (mapv #(.getAbsolutePath %))
         (mapv (partial pipeline-file recalculate router compiler)))))

(defn load-output-hashset []
  (let [file-content (with-open [rdr (io/reader "./kusachi.lock")]
                       (edn/read (new java.io.PushbackReader rdr)))]
    (reset! sha-map file-content)
    (println "Loaded prior output hashset")
    (print "Contents: ")
    (pprint/pprint @sha-map)))

(defn commit-output-hashset []
  (println "Committing output hashset")
  (with-open [w (io/writer "./kusachi.lock" :append false)]
    (.write w (prn-str @sha-map))))
