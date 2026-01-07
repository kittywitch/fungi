(ns fungi.auth.login
  (:require [fungi.components :refer [html-fragment html-ok]]
            [fungi.system :as-alias system]
            [honey.sql :as sql]
            [huff2.core :as h]
            [next.jdbc :as jdbc]
            [ring.util.anti-forgery :as anti-forgery]))

(defn login-page
  [_system _request]
  (let [user [:div {:class "row"}
                [:label {:for "username"} "Username"]
                [:input {:id "username"
                         :type "text"
                         :name "username"
                         :required true
                         :maxlength 32
                         :size 20}]]
        pass [:div {:class "row"}
                [:label {:for "password"} "Password"]
                [:input {:id "password"
                         :type "password"
                         :name "password"
                         :required true
                         :size 20}]]
        submit [:div {:class "row"}
                  [:div {:class "cell"} " "]
                  [:input {:class "button-primary"
                           :type "submit"
                           :value "Login"}]]]
    (html-ok :title "Login" :content [:form  {:class "table flex-center htmz"
                                              :method "post"
                                              :action "/login#login-form"
                                              :id "login-form"}
                                      (h/raw (anti-forgery/anti-forgery-field))
                                      [:fieldset
                                       [:legend "Log in to dork.dev"]
                                       user
                                       pass
                                       submit]])))

(defn get-user
  [db username password]
  (jdbc/execute-one!
   db
   (sql/format {:select [:*]
                :from [:users]
                :limit 1
                :where [:and [:= :username username] [:= :password password]]})))

(defn login-success
  []
  (html-fragment [:h1 "Security success"]))

(defn login-failure
  []
  (html-fragment [:h1 "Security failures"]))

(defn login-action
  [{::system/keys [db]} request]
  (let [{:keys [username password]} (:params request)]
    (let [user (get-user db username password)]
      (if (some? user)
        (login-success)
        (login-failure)))))

(defn routes
  [system]
  [["/login" {:get {:handler (partial #'login-page system)}
              :post {:handler (partial #'login-action system)}}]])
