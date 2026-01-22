(ns kusachi.rss
  (:require [clojure.data.xml :as xml]
            [clojure.pprint :as pprint]))

(def prefix-for-post "https://dork.dev/")

(defn rss-post [post]
  (let [
        {rss-form :rss
         title "title"
         rel :rel
         date "date"} post
        abs (str prefix-for-post rel)
        base [:entry {"xml:lang" "en"}
     [:title title]
     [:id abs]
     [:published date]
     [:author [:name "Kat"]]
      [:link {:rel "alternate"
       :type "text/html"
       "xml:base" abs}]
      [:content {:type "html" "xml:base" abs} rss-form]
     ]
        ]
    base))

(defn rss-feed [posts]
  (let [ xml-data (xml/sexp-as-element
                        [:feed {"xml:lang" "en"}
                         [:title "dork.dev"]
                         [:link {:rel "self"} (str prefix-for-post "atom.xml")]
                         [:link {:rel "alternate"} prefix-for-post]
                         (map (fn [[_ post]]
                                (rss-post post)
                                ) (into (sorted-map-by #(compare %2 %1)) posts))
                         ])] (xml/emit-str xml-data)))
