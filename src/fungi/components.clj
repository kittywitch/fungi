(ns fungi.components
  (:require [huff2.core :as h]))

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

(defn page-raw [& {:keys [content title]
               :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head title)
                (body content)])))

(defn page [& {:keys [content title]
               :or {title "dork.dev"}}]
  (str (h/html [:html (head title)
                (body content)])))

(defn fragment
  [content]
  (str (h/html content)))

(defn html-ok [& {:as all}]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (page all)})

(defn html-fragment
  [content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (fragment content)})

(defn htmz-link
  [text route target]
  [:a {:href (str route "#" target)
       :class "htmz"} text])
