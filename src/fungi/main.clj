(ns fungi.main
  (:require [fungi.system :as system]
            [ring.adapter.jetty :as jetty]))

(defn -main []
  (system/start-system))
