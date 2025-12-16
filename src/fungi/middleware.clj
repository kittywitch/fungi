(ns fungi.middleware
  (:require
   [ring.middleware.defaults :as middleware-defaults]))

(defn standard-html-route-middleware
  [_system]
  [#(middleware-defaults/wrap-defaults % middleware-defaults/site-defaults)])
