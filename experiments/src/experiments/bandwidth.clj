(ns experiments.bandwidth
  (:require [experiments.core]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [common.events      :as events]
            [common.service     :as service]
            [common.timer       :as timer]
            [sumo-integration.core :as sumo]
            [sumo-integration.generator :as sumo-generator]))

(def experiment-name (experiments.core/experiment-name "bandwidth"))

(defn run-simulation [t_h t_v ph speed sumo-mode]
  (let [width 2
        height 2
        event-service (events/build-esper-service experiment-name)
        simulation-cfg (sumo-generator/generate-network "/opt/sumo" "/tmp" experiment-name
                        :width width :height height :grid-length 300 :attach-length 296 :e2-length 290
                        :routes [{:id "r0/0_1" :edges "left0to0/0 0/0to1/0"}
                                 {:id "r0/0_2" :edges "0/1to0/0 0/0tobottom0"}
                                 {:id "r0/0_3" :edges "1/0to0/0 0/0toleft0"}
                                 {:id "r0/0_4" :edges "bottom0to0/0 0/0to0/1"}]
                        :flow-defs [{:route-id "r0/0_1" :throughput t_h}
                                    {:route-id "r0/0_2" :throughput t_v}
                                    {:route-id "r0/0_3" :throughput t_h}
                                    {:route-id "r0/0_4" :throughput t_v}]
                        :tls [{:id "0/0" :program-id "1" :phases [{:duration ph :state "rrrGGgrrrGGg"} {:duration ph :state "GGgrrrGGgrrr"}]}
                              {:id "0/1" :program-id "off"}
                              {:id "1/0" :program-id "off"}
                              {:id "1/1" :program-id "off"}])
        sumo-service (sumo/run-sumo event-service simulation-cfg width height sumo-mode 1000)
        timer-service (timer/run-timer event-service 1000 speed)

        bandwidth-statement (events/create-statement event-service "select avg(count) bw from DepartedVehiclesCountEvent.win:time(30 sec)")

        stop-fn #(do
                  (service/stop timer-service)
                  (service/stop sumo-service)
                  (service/stop event-service))]

    (events/subscribe event-service bandwidth-statement
      (fn [[event & _]]
        (println event)
      ))

  ))

(def cli-options
  [["-o" "--output file" "Output file name"
    :default (str experiment-name ".csv")]
   ["-s" "--speed n" "Speed-up coefficient (10 -> 10x speed-up)"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-m" "--sumo-mode mode" "Simulation mode - 'cli' or 'gui'"
    :default :cli
    :parse-fn #(keyword %)]])

(defn -main [& args]
  (let [{:keys [output speed sumo-mode]}
          (:options (cli/parse-opts args cli-options))]
    (run-simulation 0.5 0.25 30 speed sumo-mode)
  ))
