(ns kusachi.highlighting
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str]
            [clojure.zip :as zip]
            [kusachi.hickory :as fh]
            [kusachi.preprocessor :as fp]
            [kusachi.core :as kc]
            [kusachi.routing :as fr]
            [hickory.core :as hc]
            [hickory.select :as hs]))

(defn arborium-theme-router [filename]
  (str/replace filename "arborium/dist/themes/" "output/assets/css/arborium/"))

(def arborium-theme-core {:path "arborium/dist/themes/"
                :lens {:filter [(kc/glob "*.css")]
                       :remove []}
                :router [fr/resource-router arborium-theme-router]
                :compiler (fn [path out-path] (kc/copy-file-compiler path out-path))})

(def arborium-themes ["alabaster"
                      "ayu-dark"
                      "ayu-light"
                      "catppuccin-frappe"
                      "catppuccin-latte"
                      "catppuccin-macchiato"
                      "catppuccin-mocha"
                      "cobalt2"
                      "dayfox"
                      "desert256"
                      "dracula"
                      "ef-melissa-dark"
                      "github-dark"
                      "github-light"
                      "gruvbox-dark"
                      "gruvbox-light"
                      "kanagawa-dragon"
                      "light-owl"
                      "lucius-light"
                      "melange-dark"
                      "melange-light"
                      "monokai"
                      "nord"
                      "one-dark"
                      "rose-pine-moon"
                      "rustdoc-ayu"
                      "rustdoc-dark"
                      "rustdoc-light"
                      "solarized-dark"
                      "solarized-light"
                      "tokyo-night"
                      "zenburn"])

(def code-selector
  (hs/and (hs/tag :pre)
          (hs/has-child (hs/and (hs/tag :code)
                                (hs/not (hs/attr :data-lang))))))

(defn elem-maker [tag attrs content]
  {:type :element
   :tag tag
   :attrs attrs
   :content content})

; TODO: make less ugly please
(defn code-edit [elem]
  elem)

  ; (let [{attrs :attrs pre-content :content} elem
  ;       {lang :class} attrs] (if lang (let [[code] pre-content
  ;                                           {code-contents :content} code
  ;                                           [code-content] code-contents
  ;                                           arb (fp/arborium lang code-content)
  ;                                           frag-parse (map hc/as-hickory (hc/parse-fragment arb))
  ;                                           code-inner (elem-maker
  ;                                                       :code
  ;                                                       {:data-lang lang} frag-parse)]
  ;                                       (assoc elem :tag :div
  ;                                              :attrs {:class "pre-wrapper"}
  ;                                              :content [(elem-maker :pre
  ;                                                                    {:class "arborium"}
  ;                                                                    [code-inner])]))
  ;                                (let [[code] pre-content
  ;                                      new-code (assoc code :attrs {:data-lang "plaintext"})] (assoc elem :tag :div
  ;                                                                                                    :attrs {:class "pre-wrapper"}
  ;                                                                                                    :content [(elem-maker :pre
  ;                                                                                                                          {:class "arborium"}
  ;                                                                                                                          [new-code])])))))

(defn code-replacer [tree]
  (fh/hickory-update
   code-selector
   tree
   #(zip/edit % code-edit)))
