(ns fungi.config
    (:require [aero.core :refer [read-config]]
                [next.jdbc.connection :as connection]))

(defn readProfile [profile]
  (read-config "config.edn" {:profile profile}))

;; Webserver

(defn webserver-port [config]
  (get-in config [:webserver :port]))

;; Database

(defn database-type [config]
  (get-in config [:database :engine]))

(defn database-name [config]
  (get-in config [:database :name]))

(defn database-host [config]
  (get-in config [:database :host]))

(defn database-port [config]
  (get-in config [:database :port]))

(defn database-user [config]
  (get-in config [:database :user]))

(defn database-pass [config]
  (get-in config [:database :pass]))

(defn database-use-ssl [config]
  (get-in config [:database :useSSL]))

(defn jdbc-url [config]
  (connection/jdbc-url {:dbtype (database-type config)
                        :dbname (database-name config)
                        :host (database-host config)
                        :port (database-port config)
                        :username (database-user config)
                        :password (database-pass config)
                        :useSSL (database-use-ssl config)}))
