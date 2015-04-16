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

(defn run-simulation [t_h t_v ph saturation-timeout speed sumo-mode]
  (let [saturation-time (promise)
        saturation-timeout* (quot (* saturation-timeout 1000) speed)

        width 2
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

        bw-low-limit (* 1.8 (+ t_v t_h))
        bw-window-sec 30

        stop-fn #(do
                  (service/stop timer-service)
                  (service/stop sumo-service)
                  (service/stop event-service))]

    (println (str "bw-low-limit: " bw-low-limit))

    (events/create-statement event-service "create schema Bandwidth(bw double, t double)")

    (events/subscribe event-service (events/create-statement event-service (str "insert into Bandwidth select avg(count) bw, current_timestamp / 1000 as t from DepartedVehiclesCountEvent.win:time(" bw-window-sec "sec)"))
      (fn [[event & _]]
        (println event)
      ))

    (events/subscribe event-service
      (events/create-statement event-service
        (str "select * from Bandwidth match_recognize (measures current_timestamp / 1000 as t pattern (A{10}) define A as (A.bw < " bw-low-limit " and current_timestamp / 1000 > " (* bw-window-sec 2) "))"))
      (fn [[event & _]]
        (println (str "Saturation! " event))
        (deliver saturation-time (:t event))
      ))

    (let [result (deref saturation-time saturation-timeout* nil)]
      (stop-fn)
      result)
  ))

(def cli-options
  [["-o" "--output file" "Output file name"
    :default (str experiment-name ".csv")]
   ["-t" "--saturation-timeout t" "Time in seconds, after which we can state that network is not saturated"
    :default 700
    :parse-fn #(Integer/parseInt %)]
   ["-s" "--speed n" "Speed-up coefficient (10 -> 10x speed-up)"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-m" "--sumo-mode mode" "Simulation mode - 'cli' or 'gui'"
    :default :cli
    :parse-fn #(keyword %)]])

(defn -main [& args]
  (let [{:keys [output speed saturation-timeout sumo-mode]}
          (:options (cli/parse-opts args cli-options))]
    (println (run-simulation 0.25 0.125 30 saturation-timeout speed sumo-mode))
  ))
