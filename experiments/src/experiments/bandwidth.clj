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

(defn run-simulation [t_h t_v phase-length bw-window saturation-bw-threshold saturation-misses-count saturation-timeout speed sumo-mode]
  (let [saturation-time (promise)
        saturation-timeout* (+ (quot (* saturation-timeout 1000) speed) 1000)

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
                        :tls [{:id "0/0" :program-id "1" :phases [{:duration phase-length :state "rrrGGgrrrGGg"} {:duration phase-length :state "GGgrrrGGgrrr"}]}
                              {:id "0/1" :program-id "off"}
                              {:id "1/0" :program-id "off"}
                              {:id "1/1" :program-id "off"}])
        sumo-service (sumo/run-sumo event-service simulation-cfg width height sumo-mode 1000)
        timer-service (timer/run-timer event-service 1000 speed)

        bw-low-limit (* saturation-bw-threshold 2 (+ t_v t_h))

        stop-fn #(do
                  (service/stop timer-service)
                  (service/stop sumo-service)
                  (service/stop event-service))]

    (println (str "bw-low-limit: " bw-low-limit))

    (events/create-statement event-service "create schema Bandwidth(bw double, t double)")

    (events/subscribe event-service (events/create-statement event-service (str "insert into Bandwidth select avg(count) bw, current_timestamp / 1000 as t from DepartedVehiclesCountEvent.win:time(" bw-window "sec)"))
      (fn [[event & _]]
        (println event)
      ))

    (events/subscribe event-service
      (events/create-statement event-service
        (str "select * from Bandwidth match_recognize (measures current_timestamp / 1000 as t pattern (A{" saturation-misses-count "}) define A as (A.bw < " bw-low-limit " and current_timestamp / 1000 > " (* bw-window 2) "))"))
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
    :default 900
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--saturation-bw-threshold th" "Saturation bandwidth threshold, e.g. 0.8 -> 80% of expected bandwidth"
    :default 0.8
    :parse-fn #(Double/parseDouble %)]
   ["-s" "--speed n" "Speed-up coefficient (10 -> 10x speed-up)"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-w" "--bw-window w" "Time window (in seconds) used for measuring bandwidth (number of incoming vehicles is averaged over this period)"
    :default 30
    :parse-fn #(Integer/parseInt %)]
   ["-c" "--saturation-misses-count c" "Count of consecutive misses of bandwidth threshold which is required to detect saturation"
    :default 10
    :parse-fn #(Integer/parseInt %)]
   ["-p" "--phase-length p" "Length of traffic light phase in fixed phases mode, maximum length of phase in adaptive mode"
    :default 30
    :parse-fn #(Integer/parseInt %)]
   ["-m" "--sumo-mode mode" "Simulation mode - 'cli' or 'gui'"
    :default :cli
    :parse-fn #(keyword %)]])

(defn bisect [low high max-delta func]
  (let [delta (- high low)
        median (/ (+ low high) 2)]
    (println (str "low " low " high " high " delta " delta " median " median))
    (if (< delta max-delta)
      low
      (if (func median)
        (do (println (str "func(" median ") - no saturation")) (bisect median high max-delta func))
        (do (println (str "func(" median ") - saturation")) (bisect low median max-delta func))))))

(defn -main [& args]
  (let [{:keys [output phase-length bw-window saturation-bw-threshold saturation-misses-count saturation-timeout speed sumo-mode]}
          (:options (cli/parse-opts args cli-options))
        k 0.5
        low 0.1
        high 1
        max-delta 0.01
        func (fn [x] (nil? (run-simulation (* k x) x phase-length bw-window saturation-bw-threshold saturation-misses-count saturation-timeout speed sumo-mode)))
        t_v_max (bisect low high max-delta func)
        bandwidth (* 2 (+ k 1) t_v_max)]
    (println bandwidth)
  ))
