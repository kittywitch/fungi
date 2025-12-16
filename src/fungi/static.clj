(ns fungi.static
  (:require [reitit.ring :as reitit-ring]
            [ring.util.response :as response]))

(defn routes
  [_]
  [["/assets/*" (reitit-ring/create-resource-handler {:root "public/assets/"})]])
