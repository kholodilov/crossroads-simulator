(ns service.web
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [org.httpkit.server :as http-kit]
            [ring.middleware.reload :refer (wrap-reload)]
            [ring.middleware.edn :refer (wrap-edn-params)])
  (:use compojure.core
        compojure.handler
        carica.core))

(defn- response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn start-web-service [server-params state-handler events-handler query-handler]
  
  (defroutes compojure-handler
    (GET "/" [] (slurp (io/resource "public/html/index.html")))
    (GET "/state" [] (response (state-handler)))
    (GET "/events" [] events-handler)
    (GET "/query" [] query-handler)
    (route/resources "/")
    (route/files "/" {:root (config :external-resources)})
    (route/not-found "Not found!"))

  (-> compojure-handler
      site
      wrap-edn-params
      wrap-reload
      (http-kit/run-server server-params)))