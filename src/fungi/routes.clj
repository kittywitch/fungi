(ns fungi.routes
  (:require [clojure.tools.logging :as log]
            [fungi.system :as-alias system]
            [huff2.core :as h]
            [next.jdbc :as jdbc]
            [reitit.ring :as reitit-ring]))

(set! *warn-on-reflection* true)

(defn hello-handler
  [{::system/keys [db]} _request]
  (let [{:keys [planet]} (jdbc/execute-one!
                          db
                          ["SELECT 'earth' as planet"])]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str
            (h/html
             [:html
              [:body
               [:h1 (str "Hello, " planet)]]]))}))

(defn goodbye-handler
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (h/html
               [:html
                [:body [:h1 "Goodbye, world"]]]))})

(defn not-found-handler
  [_system _request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (str (h/html
               [:html
                [:body [:h1 "Not found"]]]))})

(defn routes
  [system]
  [["/" {:get {:handler (partial #'hello-handler system)}}]
   ["/goodbye" {:get {:handler (partial #'goodbye-handler system)}}]])

(defn root-handler
  [system request]
  (log/info (str (:request-method request) " - " (:uri request)))
  (let [handler (reitit-ring/ring-handler
                 (reitit-ring/router
                  (routes system))
                 #'not-found-handler)]
    (handler request)))
