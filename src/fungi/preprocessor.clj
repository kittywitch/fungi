(ns fungi.preprocessor
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.pprint :as pprint]))

(defn pandoc
  [from to toc data]
  (let [{out :out} (shell/sh "pandoc"
                             "-s"
                             "-f" from
                             "-t" to
                             "--no-highlight"
                             (when toc "--table-of-contents=true") "--template=pandoc-template.html"
                             (when toc "--toc-depth=6")
                             data
                             :dir (io/as-file "."))] out))

(defn arborium [lang input]
  (let [{out :out} (shell/sh "/home/kat/.cargo/bin/arborium"
                             "-l" lang
                             "--html"
                             :in (str input))] out))

(defn sass [in out]
  (shell/sh "sass" (str in ":" out)))
