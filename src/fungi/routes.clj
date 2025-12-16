(ns fungi.routes
  (:require [clojure.tools.logging :as log]
            [fungi.system :as-alias system]
            [honey.sql :as sql]
            [huff2.core :as h]
            [next.jdbc :as jdbc]
            [reitit.ring :as reitit-ring]
            [fungi.middleware :as middleware]
            [ring.util.anti-forgery :as anti-forgery]))

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
            [:ul [:li (link "Home" "/")
                  [:li (link "Blog" "/blog")]
                  [:li (link "External" "/external")]
                  [:li (link "Login" "/login")]]]]])

(defn footer
  []
  [:footer [:span "Written with "
            [:a {:href "https://clojure.org/"} "Clojure"]
            " in Canada! 🇨🇦"]])

(defn main
  [content]
  [:main content])

(defn htmz-frame []
  [:<>
    [:iframe {:hidden "true"
                :name "htmz"
                :onload "window.htmz(this)"}]
    [:script {:src "/assets/js/htmz.js"}]])

(defn body
  [content]
  [:body {:class "container"} [(header)
                               (main content)
                               (footer)
                               (htmz-frame)]])

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

(defn htmz-link
  [text route target]
  [:a {:href (str route "#" target)
       :class "htmz"} text])

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
                     (htmz-link "Meep" "/nicehiss" "hissy")])))

(defn login-page
  [_system _request]
  (let [{:keys [user pass submit]}
        {:user [:div {:class "row"}
                [:label {:for "username"} "Username"]
                [:input {:id "username"
                         :type "text"
                         :name "username"
                         :required true
                         :maxlength 32
                         :size 20}]]
         :pass [:div {:class "row"}
                [:label {:for "password"} "Password"]
                [:input {:id "password"
                         :type "password"
                         :name "password"
                         :required true
                         :size 20}]]
         :submit [:div {:class "row"}
                  [:div {:class "cell"} " "]
                  [:input {:class "button-primary"
                           :type "submit"
                           :value "Login"}]]}]
    (html-ok "Login" [:form  {:class "table flex-center htmz"
                              :method "post"
                              :action "/login#login-form"
                              :id "login-form"}
                      (h/raw (anti-forgery/anti-forgery-field))
                      [:fieldset
                       [:legend "Log in to dork.dev"]
                       user
                       pass
                       submit
                       ]])))

(defn get-user
  [db username password]
  (jdbc/execute-one!
   db
   (sql/format {:select [:*]
                :from [:users]
                :limit 1
                :where [:and [:= :username username] [:= :password password]] })))

(defn login-success
  []
  (html-fragment [:h1 "Security success"]))

(defn login-failure
  []
  (html-fragment [:h1 "Security failures"]))

(defn login-action
  [{::system/keys [db]} request]
  (println request)
  (let [{:keys [username password]} (:params request)]
    (let [user (get-user db username password)]
      (if (some? user)
        (login-success)
        (login-failure)))))

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
   [["/assets/*" (reitit-ring/create-resource-handler {:root "public/assets/"})]
    ["/" {:get {:handler (partial #'hello-handler system)}}]
    ["/login" {:get {:handler (partial #'login-page system)}
               :post {:handler (partial #'login-action system)}}]
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
