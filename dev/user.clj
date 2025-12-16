(ns user
  (:require [fungi.config :as configNs]
            [fungi.system :as system]
            [ragtime.next-jdbc :as jdbc]
            [ragtime.repl :as repl]))

(def system nil)

(defn start-system!
  []
  (if system
    (println "Already Started")
    (alter-var-root #'system (constantly (system/start-system)))))

(defn stop-system!
  []
  (when system
    (system/stop-system system)
    (alter-var-root #'system (constantly nil))))

(defn restart-system!
  []
  (stop-system!)
  (start-system!))

(defn server
  []
  (::system/server system))

(defn db
  []
  (::system/db system))

(defn config
  []
  (::system/config system))

(defn cookie-store
  []
  (::system/cookie-store system))

(defn ragtime-config
  [config]
  {:datastore  (jdbc/sql-database {:connection-uri (configNs/jdbc-url config)})
   :migrations (jdbc/load-resources "migrations")})

(defn systemless-config
  []
  (configNs/readProfile :dev))

(defn system-ragtime-config
  []
  (ragtime-config (config)))

(defn systemless-ragtime-config
  []
  (ragtime-config (systemless-config)))

(defn ragtime-migrate-all-system
  []
  (repl/migrate (system-ragtime-config)))

(defn ragtime-migrate-all
  []
  (repl/migrate (systemless-ragtime-config)))
