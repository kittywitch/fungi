(ns kusachi.components
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [huff2.core :as h]
            [kusachi.highlighting :as kh]))

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
                  :href "/assets/css/arborium/base.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :id "arborium-theme-light"
                  :href "/assets/css/arborium/catppuccin-latte.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :id "arborium-theme-dark"
                  :href "/assets/css/arborium/catppuccin-macchiato.css"}]
          [:title title]]])

(defn head-placeholder
  [title]
  [:head [:meta {:name "viewport"
                 :content "width=device-width, initial-scale=1"}
          [:meta {:charset "UTF-8"}]
          [:meta {:property "og:type"
                  :content "article"}]
          [:meta {:property "og:title"
                  :id "og-title-placeholder"
                  :content title}]
          [:meta {:property "og:site_name"
                  :content "dork.dev"}]
          [:meta {:property "og:locale"
                  :content "en_CA"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "/assets/css/main.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :href "/assets/css/arborium/base.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :id "arborium-theme-light"
                  :href "/assets/css/arborium/catppuccin-latte.css"}]
          [:link {:rel "stylesheet"
                  :type "text/css"
                  :id "arborium-theme-dark"
                  :href "/assets/css/arborium/catppuccin-macchiato.css"}]
          [:title {:id "placeholder"} title]]])

(def colorscheme
  [:fieldset {:class "nonoscript"}
    [:legend "Site color scheme"]
    [:input {:name "color-scheme"
             :type "radio"
             :id "color-scheme-dark"
             :value "dark"}]
    [:label {:for "color-scheme-dark"} "Dark"]
    [:input {:name "color-scheme"
             :type "radio"
             :id "color-scheme-light"
             :value "light"}]
    [:label {:for "color-scheme-light"} "Light"]
    [:button {:id "clear-color-scheme"
              :type "button"
              :title "Will also revert the page back to the color scheme preference as provided by the system and browser."} "Clear preference"]
  ]
)

(defn selectable [label lst & {id :id default :default}]
  [:<> [:label {:for id} label]
   [:select {:id id}
    (map (fn [val] (let [opt [:option {:value val} val]]
                     (if (= val default)
                       (assoc-in opt [1 :selected] "true")
                       opt
                       ))
           ) lst)
    ]])

(defn header
  []
  [:header [:details
            [:summary {:id "theme-selector"} "Theme"]
            colorscheme
            [:fieldset
             [:legend "Syntax highlighting theme"]
              (selectable "Dark preference" kh/arborium-themes :id "syntax-highlighting-dark" :default "catppuccin-macchiato")
              [:br]
              (selectable "Light preference" kh/arborium-themes :id "syntax-highlighting-light" :default "catppuccin-latte")
             ]
            ]
   [:script {:src "/assets/js/colorscheme.js"}]
   [:nav [:ul [:li {:class "logo"} (link [:img {:src "/assets/img/logo.svg"}] "/")]]
    [:ul [:li (link "Home" "/")
          [:li (link "External" "/external")]]]]])

(defn footer
  []
  [:footer [:nav [:ul
                  [:li [:span "Written with " (link [:ruby [:rb "草"] [:rt "くさ"]
                                         [:rb "地"] [:rt "ち"]
                                         [:rp "くさち"]] "https://github.com/kittywitch/kusachi") " using "
            [:a {:href "https://clojure.org/"} "Clojure"]
            " in Canada! 🇨🇦"]]]
            [:ul [:li [:a {:href "/atom.xml"} "RSS Feed"]]]]])

(defn main
  [content]
  [:main content])

(defn body
  [content]
  [:body {:class "container"} [[:div {:class "body-wrapper"} [(header)
                                                              (main content)
                                                              (footer)
                                                              [:script {:src "/assets/js/nonoscript.js"}]]]]])

(defn rubypair [kanji ruby]
  [:<> [:rb kanji]
   [:rt ruby]])

(defn image [src alt]
  [:img {:src src :alt alt}])

(defn badge-html [src alt]
  (image (str "/assets/img/88x31/" src) alt))

(defn badge
  ([src alt]
  (badge-html src alt))
  ([src alt url]
    [:a {:href url} (badge src alt)]))

(def web-badges
  [:div {:class "badge-container"}
    (badge "nixos.png" "powered by nixos" "https://nixos.org/")
    (badge "anybrowser.png" "viewable in any browser" "http://www.anybrowser.org/campaign/")
    (badge "neovim.gif" "made with neovim" "https://neovim.io/")
    (badge "librewolf-now.gif" "librewolf now!" "https://librewolf.net/")
    (badge "button-ublock.gif" "ublock origin now!" "https://ublockorigin.com/")
    (badge "searxng.png" "searxng" "https://docs.searxng.org/")
    (badge "tor.gif" "tor project" "https://www.torproject.org/")
    (badge "grapheneos.gif" "grapheneos" "https://grapheneos.org/")
    (badge "encryptyourshit.gif" "encrypt your shit!" "https://wiki.archlinux.org/title/Data-at-rest_encryption#Comparison_table")
    (badge "piracy.png" "piracy now!" "https://fmhy.net/torrenting")
    (badge "emulate.gif" "emulate now" "https://r-roms.github.io/")
    (badge "InternetPrivacy.gif" "internet privacy" "https://anonymousplanet.org/guide/")
    (badge "federate-now.jpg" "federate now!" "https://fediverse.party/en/fediverse/")
    (badge "lain.gif" "serial experiments lain" "https://nyaa.si/view/964646")
    (badge "badapple.webp" "tohou project, bad apple" "https://en.touhouwiki.net/wiki/Touhou_Wiki")
    (badge "ao3.gif" "pro ao3 freak" "https://archiveofourown.org/")
    (badge "drpepper.gif" "powered by dr pepper")
  ])

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
    ]
    [:section {:id "web-badges"}
     [:h3 {:id "web-badges"} "Web badges "
      [:small (link "Explain?" "https://en.wikipedia.org/wiki/Web_badge")]
      ]

     web-badges
    ]
    [:section {:id "contact"}
      [:h3 {:id "contact"} "Contact"]
      [:nav
        [:ul
          [:li "kat on" (link "liberachat" "https://libera.chat/")]
          [:li "@kat:kittywit.ch on Matrix"]
          [:li "kat (at) kittywit.ch on XMPP, OMEMO supported"]
          [:li "this domain has a catch-all for email, note to self, provide PGP"]
        ]
      ]
    ]
   ]
   ])

(defn postlist-for-tag [tag posts]
  (pprint/pprint posts)
  (str (h/html
        {:allow-raw true}
        [:html {:lang "en"}
         (head (str "Posts under tag " tag " - dork.dev"))
         (body [:nav [:h2 (str "Posts under tag: " tag " (" (count posts) ")")]
                [:ul [:<> (map (fn [{:strs [path title date draft]}]
                                 [:li (link [:<> (str title (when date (str " - " date))) (when draft " ") (when draft [:small "Draft"]) ]
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
                  [:p "Drafts are provided here regardless of finality because I would like to produce in the open."]
                  [:ul [:<> (map (fn [[path {:strs [title date draft]}]]
                                   [:li (link [:<> (str title (when date (str " - " date))) (when draft " ") (when draft [:small "Draft"])]
                                              (str/replace path "output/" ""))]) (into (sorted-map-by #(compare %2 %1)) posts))]]]]
        ]
    (str (h/page
           {:allow-raw true}
           [:html {:lang "en"}
            (head "dork.dev")
            (body [:<> home-page
                  [:section
                   {:id "posts"}
                   postlist
                   taglist
                   ]])]))))

(defn blogpost [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html {:lang "en"
                                         :prefix "og: https://ogp.me/ns#"}
                                  (head-placeholder title)
                                  (body [:article content])])))

(defn page-raw [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head title)
                                  (body content)])))
