(ns fungi.routes
  (:require [clojure.tools.logging :as log]
            [fungi.system :as-alias system]
            [honey.sql :as sql]
            [huff2.core :as h]
            [next.jdbc :as jdbc]
            [reitit.ring :as reitit-ring]))

(set! *warn-on-reflection* true)

(defn link
  [text url]
  [:a {:href url} text])

(defn head
  [title]
  [:head [:meta {:name "viewport"
                 :content "width=device-width, initial-scale=1"}
          [:meta {:charset "UTF-8"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "/assets/css/main.css"}]
          [:title title]]])

(defn header
  []
  [:header [:nav [:ul [:li [:h1 "dork.dev"]]]
            [:ul [:li (link "Home" "/home")
                  [:li (link "Blog" "/blog")]
                  [:Li (link "External" "/external")]]]]])

(defn footer
  []
  [:footer [:span "Written with "
            [:a {:href "https://clojure.org/"} "Clojure"]
            " in Canada! 🇨🇦"]])

(defn main
  [content]
  [:main content])

(defn htmz []
  [:iframe {:hidden "true"
            :name "htmz"
            :onload "setTimeout(()=>document.querySelector(contentWindow.location.hash||null)?.replaceWith(...contentDocument.body.childNodes))"}])

(defn body
  [content]
  [:body {:class "container"} [(header)
                               (main content)
                               (footer)
                               (htmz)]])

(defn page
  [title content]
  (str (h/html [:html (head title)
                (body content)])))

(defn fragment
  [content]
  (str (h/html content)))

(defn html-ok
  [title content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (page title content)})

(defn html-fragment
  [content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (fragment content)})

(defn hello-handler
  [{::system/keys [db]} _request]
  (let [{email :users/email} (jdbc/execute-one!
                              db
                              (sql/format {:select [:email]
                                           :from [:users]
                                           :where [:= :username "kat"]}))]
    (html-ok "nyaa" [:<> [:h1 (str "Hello, " email)]
                     [:p "awawawa,,"]
                     [:p {:id "hissy"} "This is the target for HTMZ replacement"]
                     [:a {:href "/nicehiss#hissy"
                          :target "htmz"} "SteveMRE1989Info"]])))

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
  [["/assets/*" (reitit-ring/create-resource-handler {:root "public/assets/"})]
   ["/" {:get {:handler (partial #'hello-handler system)}}]
   ["/nicehiss" {:get {:handler (partial #'nicehiss-handler system)}}]
   ["/goodbye" {:get {:handler (partial #'goodbye-handler system)}}]])

(defn root-handler
  [system request]
  (log/info (str (:request-method request) " - " (:uri request)))
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router
                  (routes system))
                 #'not-found-handler)]
    (handler request)))
