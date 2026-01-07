(ns fungi.system
  (:require [fungi.config :as config]
            [fungi.routes :as routes]
            [next.jdbc.connection :as connection]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.session.cookie :as session-cookie]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell])
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

  ; local pandoc_args = {
  ;   "-s",
  ;   "-f",
  ;   from,
  ;   "-t",
  ;   to,
  ;   "--table-of-contents="..tostring(toc),
  ;   "--template=pandoc-template.html",
  ; };
  ; if not wrap then
  ;   table.insert(pandoc_args, "--wrap=none")
  ; end

;; TODO: cheshire json parse for json type
;; - EDN transformation to allow link image matching
;; - return both out and data, e.g. [data out]
;; - in compile-markdowns, turn into json, then run transformations atop
;; - then convert into HTML from the intermediary pandoc AST JSON
(defn pandoc
  [from to toc data]
  (println data)
  (let [{out :out} (shell/sh "pandoc" "-f" from "-t" to (when toc "--table-of-contents=true") "--template=pandoc-template.html" data :dir "/home/kat/src/fungi")] (println out)))

(defn compile-markdowns
  []
  (let [grammar-matcher (.getPathMatcher
                          (java.nio.file.FileSystems/getDefault)
                          "glob:*.{md}")]
    (->> "posts"
         clojure.java.io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(.getAbsolutePath %))
         (mapv #(pandoc "markdown_mmd" "json" true %)))))

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
  (compile-markdowns)
  (let [system-so-far {::config (config/readProfile :dev)}
        system-so-far (merge system-so-far {::cookie-store (start-cookie-store)})
        system-so-far (merge system-so-far {::db (start-db system-so-far)})]
    (merge system-so-far {::server (start-server system-so-far)})))

(defn stop-system
  [system]
  (stop-server (::server system))
  (stop-db (::db system)))
