(ns fungi.preprocessor
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]))

(defn pandoc
  [from to toc data]
  (let [{out :out} (shell/sh "pandoc" "-s"
                             "-f" from
                             "-t" to
                             (when toc "--table-of-contents=true") "--template=pandoc-template.html"
                             data
                             :dir (io/as-file "."))] out))

(defn sass [in out]
  (shell/sh "sass" (str in ":" out)))
