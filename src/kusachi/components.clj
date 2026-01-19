(ns kusachi.components
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
                  :href "/assets/css/main.css"}]
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
                  :href "/assets/css/main.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "https://cdn.jsdelivr.net/npm/@arborium/arborium/dist/themes/base-rustdoc.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "https://cdn.jsdelivr.net/npm/@arborium/arborium/dist/themes/tokyo-night.css"}]
          [:title {:id "placeholder"} title]]])

(defn header
  []
  [:header [:nav [:ul [:li {:class "logo"} (link [:img {:src "/assets/img/logo.svg"}] "/")]]
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

(defn rubypair [kanji ruby]
  [:<> [:rb kanji]
   [:rt ruby]])

(def isekaijin
  [:ruby (rubypair "異" "い")
        (rubypair "世" "せ")
        (rubypair "界" "かい")
        (rubypair "人" "じん")
        [:rp "いせかいじん"]])

(def home-page
  [:<> [:h1 {:id "home"} "Home " [:ruby (rubypair "家" "うち")
                                  [:rp "うち"]]]
   [:section {:id "about"}
     [:img {:id "avatar" :src "/assets/img/vrchat.png"}]
     [:h2 {:id "about-me"} "About me"]
     [:p "Hi, I'm Kat! I'm an eccentric weirdo " isekaijin " with an eclectic set of tastes and interests, exploring them autodidactically. This website is intended to be a place where I write about doing so!"]
   [:section {:id "interests"}
      [:h3 {:id "interests"} "Interests"]
      [:ul {:class "inline-list"}
        [:li [:ruby (rubypair "日" "に")
                    (rubypair "本" "ほん")
                    (rubypair "語" "ご")
                    [:rp "にほんご"]
              ]]
        [:li "Anime and Manga"]
        [:li [:abbr {:title "Virtual reality"} "VR"]]
        [:li "Reading"]
        [:li [:abbr {:title "Software-defined Radio"} "SDR"]]
        [:li "3D Printing"]
        [:li "Mechanical keyboards"]
        [:li "NixOS"]
        [:li [:abbr {:title "Functional programming"} "FP"]]
        [:li "Electronics"]
        [:li [:abbr {:title "Political science"} "Polsci"]]
        [:li "Firearms"]
      ]
    ]]
   ])

(defn postlist-for-tag [tag posts]
  (pprint/pprint posts)
  (str (h/html
        {:allow-raw true}
        [:html
         (head (str "Posts under tag " tag " - dork.dev"))
         (body [:nav [:h2 (str "Posts under tag: " tag " (" (count posts) ")")]
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
        taglist [:section {:id "taglist"} [:nav [:h3 "Tags"]
                 (taglist tags)
                 ]]
        postlist [:section {:id "postlist"} [:nav [:h2 "Posts"]
                  [:ul [:<> (map (fn [[path {:strs [title date]}]]
                                   [:li (link (str title (when date (str " - " date)))
                                              (str/replace path "output/" ""))]) (into (sorted-map-by #(compare %2 %1)) posts))]]]]
        ]
    (str (h/html
           {:allow-raw true}
           [:html
            (head "dork.dev")
            (body [:<> home-page
                  [:section
                   {:id "posts"}
                   postlist
                   taglist
                   ]])]))))

(defn blogpost [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head-placeholder title)
                                  (body [:article content])])))

(defn page-raw [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head title)
                                  (body content)])))
