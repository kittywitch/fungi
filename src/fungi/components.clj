(ns fungi.components
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [huff2.core :as h]))

(defn link
  [text url & {:as attrs}]
  [:a (merge {:href url} attrs) text])

(defn head
  [title]
  [:head [:meta {:name "viewport"
                 :content "width=device-width, initial-scale=1"}
          [:meta {:charset "UTF-8"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "/home/kat/src/fungi/output/assets/css/main.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "https://cdn.jsdelivr.net/npm/@arborium/arborium/dist/themes/base-rustdoc.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "https://cdn.jsdelivr.net/npm/@arborium/arborium/dist/themes/tokyo-night.css"}]
          [:title title]]])

(defn head-placeholder
  [title]
  [:head [:meta {:name "viewport"
                 :content "width=device-width, initial-scale=1"}
          [:meta {:charset "UTF-8"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "/home/kat/src/fungi/output/assets/css/main.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "https://cdn.jsdelivr.net/npm/@arborium/arborium/dist/themes/base-rustdoc.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "https://cdn.jsdelivr.net/npm/@arborium/arborium/dist/themes/tokyo-night.css"}]
          [:title {:id "placeholder"} title]]])

(defn header
  []
  [:header [:nav [:ul [:li {:class "logo"} (link [:img {:src "/home/kat/src/fungi/output/assets/img/logo.svg"}] "/")]]
            [:ul [:li (link "Home" "/")
                  [:li (link "External" "/external")]]]]])

(defn footer
  []
  [:footer [:span "Written with " (link [:ruby [:rb "草"] [:rt "くさ"]
                                         [:rb "地"] [:rt "ち"]
                                         [:rp "くさち"]] "https://github.com/kittywitch/kusachi") " using "
            [:a {:href "https://clojure.org/"} "Clojure"]
            " in Canada! 🇨🇦"]])

(defn main
  [content]
  [:main content])

(defn body
  [content]
  [:body {:class "container"} [[:div {:class "body-wrapper"} [(header)
                                                              (main content)
                                                              (footer)]]]])

(defn postlist-for-tag [tag posts]
  (pprint/pprint posts)
  (str (h/html
        {:allow-raw true}
        [:html
         (head (str "Posts under tag " tag " - dork.dev"))
         (body [:nav [:h1 (str "Posts under tag: " tag " (" (count posts) ")")]
                [:ul [:<> (map (fn [{:strs [path title date]}]
                                 [:li (link (str title (when date (str " - " date)))
                                            (str/replace path "output/" ""))]) (sort-by #(get % "path") #(compare %2 %1) posts))]]])])))

(defn taglist [tags]
  (pprint/pprint tags)
  [:ul {:class "inline-list" :id "taglist"}
      [:<> (map (fn [[tag paths]]
      [:li
       (link
         [:<> tag
          [:span {:class "counter"} (count paths)]]
         (str "tags/" tag ".html"))
      ]) tags)]])

(defn postlist [posts tags]
  (let [
        taglist [:nav [:h3 "Tags"]
                 (taglist tags)
                 ]
        postlist [:nav [:h2 "Posts"]
                  [:ul [:<> (map (fn [[path {:strs [title date]}]]
                                   [:li (link (str title (when date (str " - " date)))
                                              (str/replace path "output/" ""))]) (into (sorted-map-by #(compare %2 %1)) posts))]]]
        ]
    (str (h/html
           {:allow-raw true}
           [:html
            (head "dork.dev")
            (body [:<> postlist
                   taglist
                   ])]))))

(defn blogpost [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head-placeholder title)
                                  (body [:article content])])))

(defn page-raw [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head title)
                                  (body content)])))
