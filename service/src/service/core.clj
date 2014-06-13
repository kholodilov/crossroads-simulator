(ns service.core
  (:require [org.httpkit.server :as http-kit]
            [clj-esper.core :as esper]
            [service.web :refer (start-web-service)]))

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
    (.start (Thread. #(switch-loop x y 0)))))

(defn current-state-handler []
  {:width width :height height})

(defn events-handler [request]
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

(defn -main [& args]
  (start-simulation width height)
  (start-web-service {:port 3000}
    current-state-handler
    events-handler))
