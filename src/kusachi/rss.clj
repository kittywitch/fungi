(ns kusachi.rss
  (:require [clojure.data.xml :as xml]
            [clojure.pprint :as pprint]))

(def prefix-for-post "https://dork.dev/")

(defn rss-post [post]
  (let [
        {rss-form :rss
         title "title"
        summary "summary"
         rel :rel
         date "date"} post
        abs (str prefix-for-post rel)
        base [:entry {"xml:lang" "en"}
     [:title title]
     [:id abs]
     [:published date]
     [:updated date]
     [:author [:name "Kat"]]
      [:link {:rel "alternate"
       :type "text/html"
       ;"xml:base" abs
       :href abs}]
      [:content {:type "html" "xml:base" abs} (if summary summary "Content should not be left blank, but summary was not provided. Sorry!")]
     ]
        ]
    base))

(defn rss-feed [posts]
  (let [ sorted-posts (into (sorted-map-by #(compare %2 %1)) posts)
          newest (ffirst sorted-posts)
          newest-date (get newest "date")
          xml-data (xml/sexp-as-element
                        [:feed {"xmlns" "http://www.w3.org/2005/Atom" "xml:lang" "en" }
                         [:title "dork.dev"]
                        [:id "https://dork.dev"]
                        [:updated newest-date]
                         [:link {:rel "self" :href (str prefix-for-post "atom.xml")}]
                         [:link {:rel "alternate" :href prefix-for-post}]
                         (map (fn [[_ post]]
                                (rss-post post)
                                ) sorted-posts)
                         ])] (xml/emit-str xml-data)))
