(ns experiments.core
  (:require [clojure.tools.cli :as cli]
            [common.events      :as events]
            [common.service     :as service]
            [common.timer       :as timer]
            [sumo-integration.core :as sumo]
            [sumo-integration.generator :as sumo-generator]))

(defn run-simulation [width height sumo-mode]
  (let [event-service (events/build-esper-service "Experiments")
        timer-service (timer/run-timer event-service 100)
        simulation-cfg (sumo-generator/generate-network "/opt/sumo" "/tmp" "Experiments"
                        :width width :height height :grid-length 300 :attach-length 600 :e2-length 120)
        sumo-service (sumo/run-sumo event-service simulation-cfg width height sumo-mode 500)]

    (service/build-service
      :stop-fn #(do
        (service/stop sumo-service)
        (service/stop timer-service)
        (service/stop event-service)))))

(def cli-options
  [["-w" "--width n" "Width"
    :default 2
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--height n" "Height"
    :default 2
    :parse-fn #(Integer/parseInt %)]])

(defn -main [& args]
  (let [{:keys [width height]}
          (:options (cli/parse-opts args cli-options))]
    (run-simulation width height :gui)))
