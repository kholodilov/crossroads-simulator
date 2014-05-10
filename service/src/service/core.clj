(ns service.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io])
  (:use compojure.core
        compojure.handler
        ring.middleware.edn
        carica.core))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(def counter (ref 0))

(defroutes compojure-handler
  (GET "/" [] (slurp (io/resource "public/html/index.html")))
  (GET "/req" request (str request))
  (GET "/counter" request (str
    (dosync
      (let [current @counter]
        (alter counter inc)
        (response current)
      ))))
  (route/resources "/")
  (route/files "/" {:root (config :external-resources)})
  (route/not-found "Not found!"))

(def app
  (-> compojure-handler
      site
      wrap-edn-params))
