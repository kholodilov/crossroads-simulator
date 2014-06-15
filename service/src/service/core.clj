(ns service.core
  (:require [org.httpkit.server :as http-kit]
            [clj-esper.core :as esp]
            [service.web :refer (start-web-service)]))

(def width 5)
(def height 4)
(def max-wait 30)

(esp/defevent SwitchEvent [x :int y :int t :int])

(defn switch-loop [esp-service x y t]
  (Thread/sleep (* t 1000))
  (let [new-t (rand-int max-wait)]
    (esp/trigger-event esp-service
      (esp/new-event SwitchEvent :x x :y y :t new-t))
    (recur esp-service x y new-t)))

(defn start-simulation [esp-service width height]
  (doseq [x (range width) y (range height)]
    (.start (Thread. #(switch-loop esp-service x y 0)))))

(defn current-state-handler [last-switch-events-stmt]
  {:width width :height height
   :switch-times (esp/pull-events last-switch-events-stmt)})

(defn switch-events-broadcast-fn [channel & events]
  (doseq [event events]
    (println (sort event))
    (http-kit/send! channel (pr-str event))))

(defn events-handler [switch-events-stmt request]
  (http-kit/with-channel request channel
    (let [switch-listener
          (esp/create-listener (partial switch-events-broadcast-fn channel))]

      (println "channel opened")
      (esp/attach-listener switch-events-stmt switch-listener)

      (http-kit/on-close channel
        (fn [status] 
          (esp/detach-listener switch-events-stmt switch-listener)
          (println "channel closed")))
)))

(defn -main [& args]
  (let [esp-conf (esp/create-configuration [SwitchEvent])
        esp-service (esp/create-service "CrossroadsSimulator" esp-conf)
        switch-events-stmt (esp/create-statement esp-service "select * from SwitchEvent")
        last-switch-events-stmt (esp/create-statement esp-service "select * from SwitchEvent.std:unique(x,y)")]
    (start-simulation esp-service width height)
    (start-web-service {:port 3000}
      (partial current-state-handler last-switch-events-stmt)
      (partial events-handler switch-events-stmt))))
