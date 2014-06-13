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

(defn current-state-handler []
  {:width width :height height
   :switch-times
    (vec (for [y (range height)] 
      (vec (for [x (range width)] 0))))
  })

(defn events-handler [esp-service request]
  (let [stmt (esp/create-statement esp-service "select * from SwitchEvent")]
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

      (esp/attach-listener stmt (esp/create-listener switch-listener))
    )))

(defn -main [& args]
  (let [esp-conf (esp/create-configuration [SwitchEvent])
        esp-service (esp/create-service "CrossroadsSimulator" esp-conf)]
    (start-simulation esp-service width height)
    (start-web-service {:port 3000}
      current-state-handler
      (partial events-handler esp-service))))
