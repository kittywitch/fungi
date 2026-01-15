(ns fungi.highlighting
  (:require [clojure.pprint :as pprint]
            [clojure.zip :as zip]
            [fungi.hickory :as fh]
            [fungi.preprocessor :as fp]
            [hickory.core :as hc]
            [hickory.select :as hs]))

(def code-selector
  (hs/and (hs/tag :pre)
          (hs/has-child (hs/and (hs/tag :code)
                                (hs/not (hs/attr :data-lang))))))

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

(defn elem-maker [tag attrs content]
  {:type :element
   :tag tag
   :attrs attrs
   :content content})

; TODO: make less ugly please
(defn code-edit [elem]
  (let [{attrs :attrs pre-content :content} elem
        {lang :class} attrs] (if lang (let [[code] pre-content
                                            {code-contents :content} code
                                            [code-content] code-contents
                                            arb (fp/arborium lang code-content)
                                            frag-parse (map hc/as-hickory (hc/parse-fragment arb))
                                            code-inner (elem-maker
                                                        :code
                                                        {:data-lang lang} frag-parse)]
                                        (assoc elem :tag :div
                                               :attrs {:class "pre-wrapper"}
                                               :content [(elem-maker :pre
                                                                     {:class "arborium"}
                                                                     [code-inner])]))
                                 (let [[code] pre-content
                                       new-code (assoc code :attrs {:data-lang "plaintext"})] (assoc elem :tag :div
                                                                                                     :attrs {:class "pre-wrapper"}
                                                                                                     :content [(elem-maker :pre
                                                                                                                           {:class "arborium"}
                                                                                                                           [new-code])])))))

(defn code-replacer [tree]
  (fh/hickory-update
   code-selector
   tree
   #(zip/edit % code-edit)))
