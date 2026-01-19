(ns kusachi.sass
  (:require [clojure.string :as str]
            [kusachi.preprocessor :as fp]
            [kusachi.core :as kc]
            [kusachi.routing :as fr]))

(defn scss-router [filename]
  (str/replace filename "scss/" "css/"))

(def sass-core {:path "resources/scss/"
                :lens {:filter [(kc/glob "*.scss")]
                       :remove []}
                :router [fr/resource-router scss-router (fr/refiletyper "scss" "css")]
                :compiler (fn [path out-path] (fp/sass path out-path))})
