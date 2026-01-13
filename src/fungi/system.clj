(ns fungi.system
  (:require [fungi.config :as config]
            [fungi.routes :as routes]
            [next.jdbc.connection :as connection]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.session.cookie :as session-cookie]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [clojure.zip :as zip]
            [hickory.zip :as hz]
            [hickory.core :as hic]
            [hickory.select :as hs]
            [hickory.render :refer [hickory-to-html]]
            [fivetonine.collage.util :as fcu]
            [fivetonine.collage.core :as fcc]
            [fungi.components :as fc]
            [cheshire.core :as ch]
            [clj-commons.digest :as digest]
            [clojure.edn :as edn])
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.eclipse.jetty.server Server)))

(defn pandoc
  [from to toc data]
  (let [{out :out} (shell/sh "pandoc" "-s"
                             "-f" from
                             "-t" to
                             (when toc "--table-of-contents=true") "--template=pandoc-template.html"
                             data
                             :dir "/home/kat/src/fungi")] out))

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

; https://github.com/clj-commons/hickory/issues/41#issuecomment-383893434
(defn hickory-update [selector-fn hickory-tree zip-fn]
  (loop [zip (hz/hickory-zip hickory-tree)
         next (hs/select-next-loc selector-fn zip)]
    (if next
      (let [new-zip (zip-fn next)]
        (recur new-zip (hs/select-next-loc selector-fn new-zip)))
      (zip/root zip))))

; https://clojuredocs.org/clojure.core/merge#example-5b80849ee4b00ac801ed9e75
(defn deep-merge [v & vs]
  (letfn [(rec-merge [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (if (some identity vs)
      (reduce #(rec-merge %1 %2) v vs)
      (last vs))))

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

(defn generalized-frontmatter-extractor [tree]
  (let [frontmatter-elem (hs/select (frontmatter-selector) tree)
        frontmatter (frontmatter-decode frontmatter-elem)
        clean-tree (hickory-update
                     (frontmatter-selector)
                     tree
                     zip/remove)]
    {:cleantree clean-tree
     :frontmatter frontmatter}
    ))

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
  (println content)
  (if content
    (func elem content)
    (assoc elem :tag :fungi-remove))))

(defn fungi-title-setter [elem frontmatter]
  (let [{title "title"} frontmatter]
    (println title)
    (if title
      (assoc elem :content [title " - dork.dev"] :attrs {})
      elem)))

(defn fungi-replacer [frontmatter tree]
  (let [embedded (hickory-update
                   (fungi-selector)
                   tree
                   #(zip/edit % fungi-embedder frontmatter))
        cleaned (hickory-update
                  (fungi-cleanup-selector)
                  embedded
                  zip/remove
                  )
        with-title (hickory-update
                     (hs/and (hs/tag :title)
                             (hs/id :placeholder))
                     cleaned
                     #(zip/edit % fungi-title-setter frontmatter))] with-title))


(defn output-router [filename]
  (str/replace filename "posts" "output"))

(defn simple-writer [out-path data]
  (let [parent (.getParentFile (io/as-file out-path))]
    (.mkdirs parent))
  (with-open [file-writer (io/writer out-path :append false)]
    (.write file-writer data)))

(defn add-thumb-to-filename [filename]
  (str/replace filename #"(\.[a-zA-Z0-9]+)$" ".thumb$1"))

(defn thumb-exists [file]
  (let [thumb-filename (add-thumb-to-filename (.getAbsolutePath file))]
    (.exists (io/as-file thumb-filename))))

(defn create-thumbnail
  [path out-path]
  (let [image (fcu/load-image path)
        resized (fcc/resize image :width 600)
        parent (.getParentFile (io/as-file out-path))]
          (.mkdirs parent)
          (fcu/save resized out-path :quality 0.7 :progressive true)))

(defn no-op [])

(defn glob-criteria [criteria]
  (str "glob:" criteria))

(defn glob-matcher [criteria]
  (.getPathMatcher
    (java.nio.file.FileSystems/getDefault)
    (glob-criteria criteria)))

(defn copy-file-compiler [path out-path]
  (let [file (io/as-file path)
        out-file (io/as-file out-path)
        parent (.getParentFile out-file)]
    (.mkdirs parent)
    (io/copy file out-file)))

(defn refiletyper [from to]
  (fn [file] (str/replace file from to)))

(defn glob [criteria]
   (fn [path] (.matches (glob-matcher criteria) (.getFileName (.toPath path)))))

(def post-core {:path "posts"
                :lens {:filter [(glob "*.md")]
                       :remove [(fn [_] false)]}
                :recalculate (fn [] true)
                :router [output-router (refiletyper "md" "html")]
                :compiler (fn [path out-path]
                            (->> path
                                 (pandoc "markdown" "html" true)
                                 (#(fc/blogpost :content [:hiccup/raw-html %]
                                                :title "placeholder"))
                                 (#(hic/as-hickory (hic/parse %)))
                                 ; This let may as well be considered "templateable post context".
                                 (#(let [{:keys [frontmatter cleantree]} (generalized-frontmatter-extractor %)]
                                    (fungi-replacer frontmatter cleantree)))
                                 (hickory-to-html)
                                 (simple-writer out-path)))
                })
(def image-core {:path "posts"
                 :lens {:filter [(glob "*.{png,jpg,webp,gif,bmp}")]
                        :remove [(glob "*.thumb.*")]}
                 :router [output-router]
                 :compiler (fn [path out-path] (copy-file-compiler path out-path))
                 })
(def thumb-core {:path "posts"
                 :lens {:filter [(glob "*.{png,jpg,webp,gif,bmp}")]
                        :remove [(glob "*.thumb.*") thumb-exists]}
                 :router [output-router add-thumb-to-filename]
                 :compiler (fn [path out-path] (create-thumbnail path out-path))
                 })

 (defn pipe [initial-data my-functions] ((apply comp my-functions) initial-data))

(def sha-map (atom {}))

(defn load-output-hashset []
  (let [file-content (with-open [rdr (io/reader "./fungi.lock")]
                       (edn/read (new java.io.PushbackReader rdr)))]
    (reset! sha-map file-content)
    (println "Loaded prior output hashset")
    (print "Contents: ")
    (pprint/pprint @sha-map)))

(defn pipeline-hash [path]
  (let [path-digest (digest/sha-256 (io/file path))]
    path-digest))

(defn pipeline-file [router compiler path]
  ; TODO: make this less inefficient?
  (let [out-path (pipe path router)
        current-path (.getAbsolutePath (io/file "./"))
        relative-current (relative-path current-path path)
        relative-out (relative-path current-path out-path)
        path-hash (pipeline-hash path)]
    (println (str "← " relative-current))
    (println (str "→ " relative-out))
    (swap! sha-map assoc relative-current path-hash)
    (println (str "# " path-hash))
    (let [result (compiler path out-path)]
      (println)
      (identity result))))

(defn pipeline
  [core]
  (let [{:keys [path compiler router]
         {filters :filter
          removes :remove} :lens
         :or {
              filters [(fn [] true)]
              removes [(fn [] false)]
              compiler no-op
              router [output-router]
          }
         } core
        filterer (apply every-pred filters)
        remover (apply some-fn removes)]
    (->> path
         io/file
         file-seq
         (filter #(.isFile %))
         (filter filterer)
         (remove remover)
         (mapv #(.getAbsolutePath %))
         (mapv (partial pipeline-file router compiler))
      )
    ))

(defn commit-output-hashset []
  (println "Committing output hashset")
  (with-open [w (io/writer "./fungi.lock" :append false)]
    (.write w (prn-str @sha-map))))

(defn start-system
  []
  (pprint/pprint (meta #'fc/page-raw))
  (println "Loading prior output hashset")
  (load-output-hashset)
  (println "Starting operation")
  (let [cores [post-core image-core thumb-core]]
    (mapv pipeline cores))
  (println "Finished operation")
  (commit-output-hashset)
  (shutdown-agents))
  ; (compile-markdowns)
  ; (let [system-so-far {::config (config/readProfile :dev)}
  ;       system-so-far (merge system-so-far {::cookie-store (start-cookie-store)})
  ;       system-so-far (merge system-so-far {::db (start-db system-so-far)})]
  ;   (merge system-so-far {::server (start-server system-so-far)})))

; (defn start-server
;   [{::keys [config] :as system}]
;   (jetty/run-jetty
;    (partial #'routes/root-handler system)
;    {:port  (config/webserver-port config)
;     :join? false}))

; (defn stop-server
;   [server]
;   (Server/.stop server))

; (defn start-cookie-store
;   []
;   (session-cookie/cookie-store))

; (defn stop-system
;   [system]
;   (stop-server (::server system))
;   (stop-db (::db system)))

; (defn start-db
;   [{::keys [config]}]
;   (connection/->pool HikariDataSource
;                      {:jdbcURL (config/jdbc-url config)
;                       :dbtype (config/database-type config)
;                       :username (config/database-user config)}))
                      ;;:dbname (config/database-name config)
                      ;;:host (config/database-host config)
                      ;;:port (config/database-port config)
                      ;;:password (config/database-pass config)}))

; (defn stop-db
;   [db]
;   (HikariDataSource/.close db))

