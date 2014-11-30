(ns service.web
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [org.httpkit.server :as http-kit]
            [ring.middleware.reload :refer (wrap-reload)]
            [ring.middleware.edn :refer (wrap-edn-params)]
            [common.events      :as events]
            [common.service     :as service])
  (:use compojure.core
        compojure.handler
        carica.core))

(defn- response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn max-dimension [switch-events dimension]
  (inc (apply max (or (seq (map dimension switch-events)) [0]))))

(defn current-state-handler [event-service switch-events-statement]
  (let [switch-events (events/pull-events event-service switch-events-statement)
        width (max-dimension switch-events :x)
        height (max-dimension switch-events :y)]
    {:width width :height height :switch-times switch-events}))

(defn query-results-to-channel [channel results]
  (doseq [result results]
    (http-kit/send! channel (pr-str result))))

(defn websocket-query-handler [event-service request]
  (http-kit/with-channel request channel

    (println "query: channel opened")

    (let [listener (partial query-results-to-channel channel)
          cleanup-fn (atom #())]
      (http-kit/on-receive channel
        (fn [query]
          (@cleanup-fn)
          (println (str "Starting query: " query))
          (let [statement (events/create-statement event-service query)]
            (events/subscribe event-service statement listener)
            (reset! cleanup-fn
              #(do
                (println (str "Stopping query: " query))
                (events/destroy-statement event-service statement)))
            )))

      (http-kit/on-close channel
        (fn [status]
          (@cleanup-fn)
          (println "query: channel closed")))
    )
))

(defn start-web-service [event-service server-params]
  
  (let [switch-events-statement (events/create-statement event-service "select * from SwitchEvent.std:unique(x,y)")]
  
    (defroutes compojure-handler
      (GET "/" [] (slurp (io/resource "public/html/index.html")))
      (GET "/state" [] (response (current-state-handler event-service switch-events-statement)))
      (GET "/query" [] (partial websocket-query-handler event-service))
      (route/resources "/")
      (route/files "/" {:root (config :external-resources)})
      (route/not-found "Not found!"))

    (let [stop-http-kit-fn 
            (-> compojure-handler
              site
              wrap-edn-params
              wrap-reload
              (http-kit/run-server server-params))]

      (service/build-service
        :stop-fn (fn [] 
          (events/destroy-statement event-service switch-events-statement)
          (stop-http-kit-fn))))))
