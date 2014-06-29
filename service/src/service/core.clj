(ns service.core
  (:require [org.httpkit.server :as http-kit]
            [clj-esper.core :as esp]
            [service.web :refer (start-web-service)]
            [clojure.tools.cli :refer [parse-opts]]))

(def max-wait 30)
(def states ["ns" "we"])

(defn rand-state [] (rand-nth states))
(defn flip-state [state]
  (first (clojure.set/difference (set states) (list state))))

(esp/defevent SwitchEvent [x :int y :int t :int state :string])

(defn switch-loop [esp-service x y t state]
  (if (> t 0)
    (do
      (esp/trigger-event esp-service
        (esp/new-event SwitchEvent :x x :y y :t t :state state))
      (Thread/sleep 1000)
      (recur esp-service x y (dec t) state))
    (recur esp-service x y (rand-int max-wait) (flip-state state))))

(defn start-simulation [esp-service width height]
  (let [futures
          (for [x (range width) y (range height)]
            (future (switch-loop esp-service x y 0 (rand-state))))]
    (doall futures)
    #(doseq [f futures] (future-cancel f))))

(defn current-state-handler [last-switch-events-stmt width height]
  {:width width :height height
   :switch-times (esp/pull-events last-switch-events-stmt)})

(defn query-results-to-channel [channel results]
  (doseq [result results]
    (http-kit/send! channel (pr-str result))))

(defn events-handler [switch-events-stmt request]
  (http-kit/with-channel request channel
    (let [switch-listener
          (esp/create-listener (partial query-results-to-channel channel))]

      (println "events: channel opened")
      (esp/attach-listener switch-events-stmt switch-listener)

      (http-kit/on-close channel
        (fn [status] 
          (esp/detach-listener switch-events-stmt switch-listener)
          (println "events: channel closed")))
)))

(defn query-handler [esp-service request]
  (http-kit/with-channel request channel

    (println "query: channel opened")

    (let [listener (esp/create-listener (partial query-results-to-channel channel))
          cleanup-fn (atom #())]
      (http-kit/on-receive channel
        (fn [query]
          (@cleanup-fn)
          (println (str "Starting query: " query))
          (let [stmt (esp/create-statement esp-service query)]
            (esp/attach-listener stmt listener)
            (reset! cleanup-fn
              #(do
                (println (str "Stopping query: " query))
                (esp/detach-listener stmt listener)))
            )))

      (http-kit/on-close channel
        (fn [status]
          (@cleanup-fn)
          (println "query: channel closed")))
    )
))

(defn run [width height]
  (let [esp-conf (esp/create-configuration [SwitchEvent])
        esp-service (esp/create-service "CrossroadsSimulator" esp-conf)
        switch-events-stmt (esp/create-statement esp-service "select * from SwitchEvent")
        last-switch-events-stmt (esp/create-statement esp-service "select * from SwitchEvent.std:unique(x,y)")
        stop-simulation (start-simulation esp-service width height)
        stop-web-service     
          (start-web-service {:port 3000}
            (partial current-state-handler last-switch-events-stmt width height)
            (partial events-handler switch-events-stmt)
            (partial query-handler esp-service))]
    #(do (stop-web-service) (stop-simulation) (.destroy esp-service))
))

(def cli-options
  [["-w" "--width n" "Width"
    :default 4
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--height n" "Height"
    :default 3
    :parse-fn #(Integer/parseInt %)]])

(defn -main [& args]
  (let [{:keys [options]} (parse-opts args cli-options)]
    (run (options :width) (options :height))))
