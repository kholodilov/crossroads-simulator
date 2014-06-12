(ns service.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [org.httpkit.server :as http-kit]
            [ring.middleware.reload :refer (wrap-reload)]
            [ring.middleware.edn :refer (wrap-edn-params)]
            [clj-esper.core :as esper])
  (:use compojure.core
        compojure.handler
        carica.core))

(def width 5)
(def height 4)
(def max-wait 30)

(esper/defevent SwitchEvent [x :int y :int t :int])

(def esp-service (esper/create-service "CrossroadsSimulator"
                   (esper/create-configuration [SwitchEvent])))

(defn switch-loop [x y t]
  (Thread/sleep (* t 1000))
  (let [new-t (rand-int max-wait)]
    (esper/trigger-event esp-service
      (esper/new-event SwitchEvent :x x :y y :t new-t))
    (recur x y new-t)))

(defn start-simulation [width height]
  (doseq [x (range width) y (range height)]
    (future (switch-loop x y (rand-int max-wait)))))

(defn events-websocket-handler [request]
  (let [stmt (esper/create-statement esp-service "select * from SwitchEvent")]
    (http-kit/with-channel request channel
      (println "channel opened")
      (http-kit/on-receive channel (fn [data]))

      (http-kit/on-close channel
        (fn [status]
          (.destroy stmt)
          (println "channel closed")))

      (defn switch-listener [& events]
        (doseq [event events]
          (println (sort event))
          (http-kit/send! channel (pr-str event))))

      (esper/attach-listener stmt (esper/create-listener switch-listener))
    )))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

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
