(ns kusachi.server
  (:require [compojure.core :as cc]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as rj]))

(cc/defroutes app-routes
  (route/files "/" {:root "./output"}))

(defn serve []
  (rj/run-jetty app-routes {:port 3000}))
