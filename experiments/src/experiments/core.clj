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
                        :width width :height height :grid-length 300 :attach-length 300 :e2-length 120
                        :routes [{:id "r0/0_1" :edges "left0to0/0 0/0to1/0"}
                                 {:id "r0/0_2" :edges "0/1to0/0 0/0tobottom0"}
                                 {:id "r0/0_3" :edges "1/0to0/0 0/0toleft0"}
                                 {:id "r0/0_4" :edges "bottom0to0/0 0/0to0/1"}]
                        :vehicles-defs [{:route-id "r0/0_1" :count 5}
                                        {:route-id "r0/0_2" :count 5}
                                        {:route-id "r0/0_3" :count 5}
                                        {:route-id "r0/0_4" :count 5}]
                        :tls [{:id "0/0" :program-id "1" :phases [{:duration 7 :state "rrrGGgrrrGGg"} {:duration 14 :state "GGgrrrGGgrrr"}]}
                              {:id "0/1" :program-id "off"}
                              {:id "1/0" :program-id "off"}
                              {:id "1/1" :program-id "off"}])
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
