(ns fungi.config
    (:require [aero.core :refer [read-config]]))

(defn readProfile [profile]
  (read-config "config.edn" {:profile profile}))

;; Webserver

(defn webserver-port [config]
  (get-in config [:webserver :port]))

;; Database

(defn database-name [config]
  (get-in config [:database :name]))

(defn database-user [config]
  (get-in config [:database :user]))

(defn database-host [config]
  (get-in config [:database :host]))

(defn database-port [config]
  (get-in config [:database :port]))

(defn database-pass [config]
  (get-in config [:database :pass]))
