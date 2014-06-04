(ns service.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [org.httpkit.server :as http-kit]
            [ring.middleware.reload :refer (wrap-reload)]
            [ring.middleware.edn :refer (wrap-edn-params)]
            [service.esper :as esper])
  (:use compojure.core
        compojure.handler
        carica.core))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(def width 5)
(def height 4)
(def max-wait 30)

(def switch-event
  (esper/new-event "Switch"
    {"x" :int
     "y" :int
     "t" :int}))

(def esp-service (esper/create-service "CrossroadsSimulator"
                   (esper/configuration switch-event)))

(defn send-event
  [service event event-type]
  (.sendEvent (.getEPRuntime service) event event-type))

(defn switch-loop [x y t]
  (Thread/sleep (* t 1000))
  (let [new-t (rand-int max-wait)]
    (send-event esp-service
      {"x" x "y" y "t" new-t} "Switch")
      (recur x y new-t)))

(defn start-simulation [width height]
  (doseq [x (range width) y (range height)]
    (future (switch-loop x y (rand-int max-wait)))))

(defn events-websocket-handler [request]
  (http-kit/with-channel request channel
    (println "channel opened")
    (http-kit/on-receive channel (fn [data]))

    (def stmt (esper/new-statement esp-service "select * from Switch"))

    (http-kit/on-close channel
      (fn [status]
        (esper/destroy-statement stmt)
        (println "channel closed")))

    (defn switch-listener [new-events]
      (let [event (first new-events)
            [x y t] (map #(.get event %) ["x" "y" "t"])]
        (println (str x " " y " " t))
        (http-kit/send! channel
          (pr-str {:x x :y y :value t}))))

    (esper/add-listener stmt (esper/create-listener switch-listener))
))

(defroutes compojure-handler
  (GET "/" [] (slurp (io/resource "public/html/index.html")))
  (GET "/size" [] (response {:width width :height height}))
  (GET "/events-ws" [] events-websocket-handler)
  (route/resources "/")
  (route/files "/" {:root (config :external-resources)})
  (route/not-found "Not found!"))

(defn -main [& args]
  (start-simulation width height)
  (-> compojure-handler
      site
      wrap-edn-params
      wrap-reload
      (http-kit/run-server {:port 3000})))
