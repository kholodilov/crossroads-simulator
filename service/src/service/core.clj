(ns service.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [org.httpkit.server :as http-kit]
            [ring.middleware.reload :refer (wrap-reload)]
            [ring.middleware.edn :refer (wrap-edn-params)])
  (:use compojure.core
        compojure.handler
        carica.core))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(def counter (atom 0))

(defn inc-counter-loop []
  (Thread/sleep 1000)
  (swap! counter inc)
  (recur))

(def width 5)
(def height 4)

(defn counter-websocket-handler [request]
  (http-kit/with-channel request channel
    (if (http-kit/websocket? channel)
      (println "WebSocket channel")
      (println "HTTP channel"))
    (http-kit/on-close channel
      (fn [status]
        (remove-watch counter channel)
        (println "channel closed")))
    (http-kit/on-receive channel (fn [data]))
    (add-watch counter channel
      (fn [_channel _counter old-cnt new-cnt]
        (http-kit/send! channel (str new-cnt))))
))

(defroutes compojure-handler
  (GET "/" [] (slurp (io/resource "public/html/index.html")))
  (GET "/size" [] (response {:width width :height height}))
  (GET "/counter" request
    (swap! counter inc)
    (str (response @counter)))
  (GET "/counter-ws" [] counter-websocket-handler)
  (route/resources "/")
  (route/files "/" {:root (config :external-resources)})
  (route/not-found "Not found!"))

(defn -main [& args]
  (future (inc-counter-loop))
  (-> compojure-handler
      site
      wrap-edn-params
      wrap-reload
      (http-kit/run-server {:port 3000})))
