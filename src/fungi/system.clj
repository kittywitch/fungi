(ns fungi.system
  (:require [fungi.routes :as routes]
            [next.jdbc.connection :as connection]
            [ring.adapter.jetty :as jetty]
            [fungi.config :as config])
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

(defn start-server
  [{::keys [config] :as system}]
  (jetty/run-jetty
   (partial #'routes/root-handler system)
   {:port  (config/webserver-port config)
    :join? false}))

(defn stop-server
  [server]
  (Server/.stop server))

(defn start-system
  []
  (let [system-so-far {::config (config/readProfile :dev)}
        system-so-far (merge system-so-far {::db (start-db system-so-far)})]
    (merge system-so-far {::server (start-server system-so-far)})))

(defn stop-system
  [system]
  (stop-server (::server system))
    (stop-db (::db system)))
