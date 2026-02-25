(ns kusachi.components
  (:require [clojure.java.io :as io]
            [toml-clj.core :as toml]
            [clojure.string :as str]
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
            ]
   [:script {:src "/assets/js/colorscheme.js"}]
   [:nav [:ul [:li {:class "logo"} (link [:img {:src "/assets/img/logo.svg" :alt "A witch hat with ears"}] "/")]]
    [:ul [:li (link "Home" "/")]
          [:li (link "Stuff I'd like to share" "/external.html")]]]])

(defn rubypair [kanji ruby]
  [:<> [:rb kanji]
   [:rp "("][:rt ruby] [:rp ")"]])

(defn footer
  []
  [:footer [:nav [:ul
                  [:li [:span "Written with " (link [:ruby (rubypair "草" "くさ")
                                         (rubypair "地" "ち")
                                         ] "https://github.com/kittywitch/kusachi") " using "
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
    (badge "html5.png" "Valid HTML5!" "https://validator.w3.org/nu/?level=warning&doc=https%3A%2F%2Fdork.dev")
    (badge "vcss.png" "Valid CSS!" "https://jigsaw.w3.org/css-validator/check/referer")
    (badge "valid-atom.png" "Valid Atom 1.0" "https://validator.w3.org/feed/check.cgi?url=https%3A//dork.dev/atom.xml")
    (badge "nixos.png" "powered by nixos" "https://nixos.org/")
    (badge "nginx.png" "nginx powered" "https://nginx.org/")
    (badge "anybrowser.png" "viewable in any browser" "http://www.anybrowser.org/campaign/")
    (badge "neovim.gif" "made with neovim" "https://neovim.io/")
    (badge "ISO.png" "ISO 8601 dates" "https://www.w3.org/QA/Tips/iso-date")
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
    (badge "anarchy.gif" "anarchy now" "https://bookshelf.theanarchistlibrary.org/library/librarian-picks")
    (badge "lain.gif" "serial experiments lain" "https://nyaa.si/view/964646")
    (badge "badapple.webp" "tohou project, bad apple" "https://en.touhouwiki.net/wiki/Touhou_Wiki")
    (badge "ao3.gif" "pro ao3 freak" "https://archiveofourown.org/")
    (badge "DIY.png" "DIY HRT!" "https://diyhrt.market/")
    (badge "anime.webp" "anime is gay as hell but I approve")
    (badge "drpepper.gif" "powered by dr pepper")
  ])

(def isekaijin
  [:ruby (rubypair "異" "い")
        (rubypair "世" "せ")
        (rubypair "界" "かい")
        (rubypair "人" "じん")])

(def home-page
  [:<> [:h1 {:id "home"} "Home " [:ruby (rubypair "家" "うち")]]
   [:section {:id "about"}
     [:img {:id "avatar" :alt "A purple-haired catgirl with glasses and a nix flake in her hair" :src "/assets/img/vrchat.png"}]
     [:h2 {:id "about-me"} "About me"]
     [:p "Hi, I'm Kat! I'm an eccentric weirdo " isekaijin " with an eclectic set of tastes and interests, exploring them autodidactically. This website is intended to be a place where I write about doing so!"]
   [:section {:id "interests-sec"}
      [:h3 {:id "interests"} "Interests"]
      [:ul {:class "inline-list"}
        [:li [:ruby (rubypair "日" "に")
                    (rubypair "本" "ほん")
                    (rubypair "語" "ご")
              ]]
        [:li "Anime and Manga"]
        [:li [:abbr {:title "Virtual reality"} "VR"]]
        [:li "Reading"]
        [:li [:abbr {:title "Software-defined Radio"} "SDR"]]
        [:li "3D Printing"]
        [:li "Mechanical keyboards"]
        [:li "NixOS"]
        [:li "Guix System"]
        [:li [:abbr {:title "Functional programming"} "FP"]]
        [:li "Lisp dialects"]
        [:li "Electronics"]
        [:li [:abbr {:title "Political science"} "Polsci"]]
        [:li "Firearms"]
      ]
    ]
    [:section {:id "web-badges-sec"}
     [:h3 {:id "web-badges"} "Web badges "
      [:small (link "Explain?" "https://en.wikipedia.org/wiki/Web_badge")]
      ]

     web-badges
    ]
    [:section {:id "contact-sec"}
      [:h3 {:id "contact"} "Contact"]
      [:nav
        [:ul
          [:li "kat on " (link "liberachat" "https://libera.chat/")]
          [:li "@kat:kittywit.ch on Matrix"]
          [:li "kat (at) kittywit.ch on XMPP, OMEMO supported"]
          [:li "kat (at) kittywit.ch via email"]
        ]
      ]
    ]
   ]
   ])

(defn external-page []
  (let [
    external (with-open [rdr (clojure.java.io/reader "resources/external.toml")]
      (toml/read rdr))
    categories (get external "categories")
  ]
  (pprint/pprint categories)
  (str (h/page
        {:allow-raw true}
        [:html {:lang "en"}
         (head "Stuff I'd like to share")
         (body [:nav [:h2 "Stuff I'd like to share"]
            (map (fn [{:strs [title note posts] :as all}]
              (pprint/pprint all)
              [:<>
                [:h3 title]
                (when note [:p note])
                [:ul
                (map (fn [{:strs [title author permalink archive suffix]}]
                  [:li
                    [:article (link [:strong title] permalink) (when author [:<> " by " author])
                      (when archive [:<> " - " (link "Archive" archive)])
                      (when suffix [:<> " (" suffix ")"])
                    ]
                  ]
                  ) posts)
                ]]) categories)])]))))

(defn post-for-list-tag [{:strs [path title date draft summary cws] :as all}]
  (let [post-content [:li
    [:article
      [:h2 (link title (str/replace path "output/" ""))
           (when draft " ") (when draft [:small "Draft"])
            (when cws "  ") (when cws [:small "Content warnings"])]
      (when date [:time {:datetime date} date])
      [:p summary]
    ]
  ]]
  (pprint/pprint all)
  (pprint/pprint post-content)
  post-content
))

(defn post-for-list [[path post]]
  (let [{:strs [title date draft cws summary]} post
    post-content
  [:li
    [:article
      [:h3 (link title (str/replace path "output/" ""))
           (when draft " ") (when draft [:small "Draft"])
            (when cws "  ") (when cws [:small [:abbr {:title "Content warnings"} "CWs"]])]
      (when date [:<> [:span "Posted on: "] [:time {:datetime date} date]])
      (when summary [:p summary])
    ]
  ]]
  post-content
))

(defn postlist-for-tag [tag posts]
  (str (h/page
        {:allow-raw true}
        [:html {:lang "en"}
         (head (str "Posts under tag " tag " - dork.dev"))
         (body [:nav [:h2 (str "Posts under tag: " tag " (" (count posts) ")")]
                [:ul [:<> (map post-for-list-tag (sort-by #(get % "path") #(compare %2 %1) posts))]]])])))

(defn taglist [tags]
  [:ul {:class "inline-list" :id "taglist"}
      [:<> (map (fn [[tag paths]]
      [:li
       (link
         [:<> tag
          [:span {:class "counter"} (count paths)]]
         (str/replace (str "tags/" tag ".html") " " "%20"))
      ]) tags)]])



(defn postlist [posts tags]
  (let [
        taglist [:section {:id "taglist-sec"} [:h3 "Tags"] [:nav
                 (taglist tags)
                 ]]
        postlist [:section {:id "postlist-sec"} [:h2 "Posts"] [:nav
                  [:p "Drafts are provided here regardless of finality because I would like to produce in the open."]
                  [:ul [:<> (map post-for-list (into (sorted-map-by #(compare %2 %1)) posts))]]]]
        ]
    (str (h/page
           {:allow-raw true}
           [:html {:lang "en"}
            (head "dork.dev")
            (body [:<> home-page
                   postlist
                   taglist
                   ])]))))

(defn blogpost [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/page {:allow-raw true} [:html {:lang "en"
                                         :prefix "og: https://ogp.me/ns#"}
                                  (head-placeholder title)
                                  (body [:article content])])))

(defn page-raw [& {:keys [content title]
                   :or {title "dork.dev"}}]
  (str (h/html {:allow-raw true} [:html (head title)
                                  (body content)])))
