(ns fungi.routes
  (:require [clojure.tools.logging :as log]
            [fungi.auth.login :as auth-login]
            [fungi.components :refer [htmz-link html-ok html-fragment]]
            [fungi.middleware :as middleware]
            [fungi.static :as static]
            [fungi.system :as-alias system]
            [honey.sql :as sql]
            [huff2.core :as h]
            [next.jdbc :as jdbc]
            [reitit.ring :as reitit-ring]))

(set! *warn-on-reflection* true)

(defn hello-handler
  [{::system/keys [db]} _request]
  (let [{email :users/email} (jdbc/execute-one!
                              db
                              (sql/format {:select [:email]
                                           :from [:users]
                                           :where [:= :username "kat"]}))]
    (html-ok :title "nyaa" :content [:<> [:h1 (str "Hello, " email)]
                                     [:p "awawawa,,"]
                                     [:p {:id "hissy"} "This is the target for HTMZ replacement"]
                                     (htmz-link "Meep" "/nicehiss" "hissy")])))

(defn nicehiss-handler
  [_system _request]
  (html-fragment [:h1 "Let's get that out on a tray"]))

(defn goodbye-handler
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (h/html
               [:html
                [:body [:h1 "Goodbye, world"]]]))})

(defn not-found-handler
  [_system _request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (str (h/html
               [:html
                [:body [:h1 "Not found"]]]))})

(defn routes
  [system]
  [""
   {:middleware (middleware/standard-html-route-middleware system)}
   [(auth-login/routes system)
    (static/routes system)
    ["/" {:get {:handler (partial #'hello-handler system)}}]
    ["/nicehiss" {:get {:handler (partial #'nicehiss-handler system)}}]
    ["/goodbye" {:get {:handler (partial #'goodbye-handler system)}}]]])

(defn root-handler
  [system request]
  (log/info (str (:request-method request) " - " (:uri request)))
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router
                  (routes system))
                 #'not-found-handler)]
    (handler request)))
