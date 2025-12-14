(ns user
  (:require [fungi.system :as system]
            [fungi.config :as configNs]
            [ragtime.next-jdbc :as jdbc]))

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

(defn ragtimeConfig
  []
  {:datastore  (jdbc/sql-database {:connection-uri (configNs/jdbc-url (config))})
   :migrations (jdbc/load-resources "migrations")})
