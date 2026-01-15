(ns fungi.components
  (:require [huff2.core :as h]
            [clojure.string :as str]
            ))

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
                  :href "/home/kat/src/fungi/output/assets/css/main.css"}]
          [:title title]]])

(defn head-placeholder
  [title]
  [:head [:meta {:name "viewport"
                 :content "width=device-width, initial-scale=1"}
          [:meta {:charset "UTF-8"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "/home/kat/src/fungi/output/assets/css/main.css"}]
          [:title {:id "placeholder"} title]]])

(defn header
  []
  [:header [:nav [:ul [:li {:class "logo"} (link [:img {:src "/home/kat/src/fungi/output/assets/img/logo.svg"}] "/")]]
            [:ul [:li (link "Home" "/")
                  [:li (link "External" "/external")]
                  ]]]])

(defn footer
  []
  [:footer [:span "Written with " (link [:ruby [:rb "草"] [:rt "くさ"]
                                        [ :rb "地"] [:rt "ち"]
                                        [:rp "くさち"]] "https://github.com/kittywitch/kusachi")" using "
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

(defn postlist [posts]
  (str (h/html
         {:allow-raw true}
         [:html (head "dork.dev")
          (body [:nav [:h1 "Posts"]
                 [:ul [:<> (map (fn [[path {:strs [title date]}]]
                                       [:li (link (str title (when date (str " - " date)))
                                                  (str/replace path "output/" ""))]
                                       ) posts)]]])])))

(defn blogpost [& {:keys [content title]
               :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head-placeholder title)
                (body [:article content])])))

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
