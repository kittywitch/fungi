(ns fungi.middleware
  (:require [fungi.system :as-alias system]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [ring.middleware.default-charset :refer [wrap-default-charset]]
   [ring.middleware.flash :refer [wrap-flash]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.nested-params :refer [wrap-nested-params]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [ring.middleware.x-headers :as x]
   [ring.middleware.defaults :as middleware-defaults]))

(defn standard-html-route-middleware
  [{::system/keys [cookie-store]}]
  [#(x/wrap-content-type-options % :nosniff)
   #(x/wrap-frame-options % :sameorigin)
   wrap-not-modified
   #(wrap-default-charset % "utf-8")
   wrap-content-type
   wrap-cookies
   wrap-params
   wrap-multipart-params
   wrap-nested-params
   wrap-keyword-params
   #(wrap-session % {:cookie-attrs {:http-only true}
                     :store cookie-store})
   wrap-flash
   wrap-anti-forgery])
