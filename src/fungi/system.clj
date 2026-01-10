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
            [fivetonine.collage.core :as fcc])
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.eclipse.jetty.server Server)))

(defn start-db
  [{::keys [config]}]
  (connection/->pool HikariDataSource
                     {:jdbcURL (config/jdbc-url config)
                      :dbtype (config/database-type config)
                      :username (config/database-user config)}))
                      ;;:dbname (config/database-name config)
                      ;;:host (config/database-host config)
                      ;;:port (config/database-port config)
                      ;;:password (config/database-pass config)}))

(defn stop-db
  [db]
  (HikariDataSource/.close db))

;; TODOs:
;; - Move away from thumb system, make img -> a>img as a transformer
;; - Use a parser combinator library or instaparse to provide an emmet parser
;; -- Emmet tree to huff/hiccup/hickory structure to freeload on HTML generation
;; - Move away from hybrid approach, go all in on static site generation
;; - Provide specification of the way something should be compiled, defer computation
;; -- e.g. allow one to provide the route transformer, but only use the "route" termination
;;    at the point of compilation completion, by the same means provide the compilation transformer
;;    but defer it until necessary

(defn pandoc
  [from to toc data]
  (println data)
  (let [{out :out} (shell/sh "pandoc" "-s" "-f" from "-t" to (when toc "--table-of-contents=true") "--template=pandoc-template.html" data :dir "/home/kat/src/fungi")] out))

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

(defn is-thumb-already [filename]
  (not (str/includes? filename ".thumd")))

(defn select-thumbable
  []
  (hs/child (hs/tag :a) (hs/and (hs/tag :img)
                                (hs/attr :src is-thumb-already)
                                hs/first-child
                                hs/last-child)))

(defn rename-thumb [elem]
  (let [{{filename :src} :attrs} elem]
    (println filename)
    (let [new-filename (str/replace filename "thumb" "thumd")]
      (println new-filename)
      (let [result (deep-merge elem {:attrs {:src new-filename}})]
        (println result)
        result))))

(defn replace-thumb [tree]
  (hickory-update
    (select-thumbable)
    tree
    #(zip/edit % rename-thumb)))

(defn output-router [filename]
  (str/replace filename "posts" "output"))

(defn simple-writer [out-path data]
  (let [parent (.getParentFile (io/as-file out-path))]
    (.mkdirs parent))
  (with-open [file-writer (io/writer out-path :append false)]
    (.write file-writer data)))


(defn compile-markdowns
  []
  (let [grammar-matcher (.getPathMatcher
                          (java.nio.file.FileSystems/getDefault)
                          "glob:*.{md}")]
    (->> "posts"
         io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(.getAbsolutePath %))
         (mapv #(pandoc "markdown_mmd" "html" true %))
         (mapv #(hic/as-hickory (hic/parse %)))
         (mapv replace-thumb)
         )))

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
          (println (str "Running upon " path " will be " out-path))
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
                :router [output-router (refiletyper "md" "html")]
                :compiler (fn [path out-path] (->> path
                                                   (pandoc "markdown" "html" true)
                                                   ;;#(hic/as-hickory (hic/parse %))
                                                   ;;(replace-thumb)
                                                   ;;(hickory-to-html)
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

(defn pipeline-file [router compiler path]
  (pprint/pprint router)
  (pprint/pprint compiler)
  (pprint/pprint path)
  (let [out-path (pipe path router)]
    (pprint/pprint (str "Pipeline for file " path " to " out-path))
    (compiler path out-path)))

(defn pipeline
  [core]
  (pprint/pprint core)
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
(defn start-server
  [{::keys [config] :as system}]
  (jetty/run-jetty
   (partial #'routes/root-handler system)
   {:port  (config/webserver-port config)
    :join? false}))

(defn stop-server
  [server]
  (Server/.stop server))

(defn start-cookie-store
  []
  (session-cookie/cookie-store))

(defn start-system
  []
  (let [cores [post-core image-core thumb-core]]
    (mapv pipeline cores)))
  ; (compile-markdowns)
  ; (let [system-so-far {::config (config/readProfile :dev)}
  ;       system-so-far (merge system-so-far {::cookie-store (start-cookie-store)})
  ;       system-so-far (merge system-so-far {::db (start-db system-so-far)})]
  ;   (merge system-so-far {::server (start-server system-so-far)})))

(defn stop-system
  [system]
  (stop-server (::server system))
  (stop-db (::db system)))
