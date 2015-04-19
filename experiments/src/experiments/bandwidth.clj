(ns experiments.bandwidth
  (:require [experiments.core]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [common.events      :as events]
            [common.service     :as service]
            [common.timer       :as timer]
            [sumo-integration.core :as sumo]
            [sumo-integration.generator :as sumo-generator]
            [switchlights-control.core :as switchlights]))

(def experiment-name (experiments.core/experiment-name "bandwidth"))

(defn run-simulation [flow-h flow-v {:keys [tls-adaptive phase-length bw-window saturation-bw-threshold saturation-misses-count saturation-timeout speed sumo-mode]}]
  (let [saturation-time (promise)
        saturation-timeout* (+ (quot (* saturation-timeout 1000) speed) 1000)
        bw-low-limit (* saturation-bw-threshold 2 (+ flow-v flow-h))

        width 2
        height 2
        event-service (events/build-esper-service experiment-name)
        simulation-cfg (sumo-generator/generate-network "/opt/sumo" "/tmp" experiment-name
                        :width width :height height :grid-length 300 :attach-length 296 :e2-length 120
                        :routes [{:id "r0/0_1" :edges "left0to0/0 0/0to1/0"}
                                 {:id "r0/0_2" :edges "0/1to0/0 0/0tobottom0"}
                                 {:id "r0/0_3" :edges "1/0to0/0 0/0toleft0"}
                                 {:id "r0/0_4" :edges "bottom0to0/0 0/0to0/1"}]
                        :flow-defs [{:route-id "r0/0_1" :flow flow-h}
                                    {:route-id "r0/0_2" :flow flow-v}
                                    {:route-id "r0/0_3" :flow flow-h}
                                    {:route-id "r0/0_4" :flow flow-v}]
                        :tls [
                               (if tls-adaptive
                                 {:id "0/0" :program-id "off"}
                                 {:id "0/0" :program-id "1" :phases [{:duration phase-length :state "rrrGGgrrrGGg"} {:duration phase-length :state "GGgrrrGGgrrr"}]})
                               {:id "0/1" :program-id "off"} 
                               {:id "1/0" :program-id "off"}
                               {:id "1/1" :program-id "off"}
                             ])
        sumo-service (sumo/run-sumo event-service simulation-cfg width height sumo-mode 1000)

        switchlights-params
                      {:phase-length-mode "controlled"
                       :phase-length-update-mode "on-switch"
                       :phase-length-update-frequency nil}
        switchlights-service (if tls-adaptive
                               (switchlights/run-switchlights event-service width height phase-length switchlights-params)
                               (service/noop-service))

        timer-service (timer/run-timer event-service 1000 speed)

        stop-fn #(do
                  (service/stop timer-service)
                  (service/stop switchlights-service)
                  (service/stop sumo-service)
                  (service/stop event-service))]

    (println (str "bw-low-limit: " bw-low-limit))

    (events/create-statement event-service "create schema Bandwidth(bw double, t double)")

    (events/subscribe event-service (events/create-statement event-service (str "insert into Bandwidth select avg(count) bw, current_timestamp / 1000 as t from DepartedVehiclesCountEvent.win:time(" bw-window "sec)"))
      (fn [[event & _]]
        (println event)
      ))

    ; (events/subscribe event-service (events/create-statement event-service (str "select current_timestamp / 1000 as t, count from DepartedVehiclesCountEvent"))
    ;   (fn [[event & _]]
    ;     (println (str "Departed: " event))
    ;   ))

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
  [["-o" "--console-output" "Output only to console"]
   ["-a" "--tls-adaptive" "Enable TLS adaptive mode"]
   ["-t" "--saturation-timeout t" "Time in seconds, after which we can state that network is not saturated"
    :default 900
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--saturation-bw-threshold th" "Saturation bandwidth threshold, e.g. 0.8 -> 80% of expected bandwidth"
    :default 0.8
    :parse-fn #(Double/parseDouble %)]
   ["-r" "--ratio-min r" "Min flows ratio in iteration"
    :default 0.02
    :parse-fn #(Double/parseDouble %)]
   ["-R" "--ratio-max R" "Max flows ratio in iteration"
    :default 1.0
    :parse-fn #(Double/parseDouble %)]
   ["-e" "--ratio-step e" "Flows ratio step in iteration"
    :default 0.01
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

(defn find-bandwidth [flows-ratio options]
  (println (str "### Finding maximal bandwidth for flows ratio = " flows-ratio))
  (let [flow-low 0.1
        flow-high 1
        max-delta 0.01
        not-saturated-fn? (fn [x] (nil? (run-simulation (* flows-ratio x) x options)))
        flow-v-max (bisect flow-low flow-high max-delta not-saturated-fn?)
        bandwidth (* 2 (+ flows-ratio 1) flow-v-max)]
    bandwidth
  ))

(defn -main [& args]
  (let [{:keys [console-output ratio-min ratio-max ratio-step] :as options}
          (:options (cli/parse-opts args cli-options))
        output (if console-output
                  System/out
                  (str experiment-name ".csv"))]
    (with-open [w (io/writer output)]
      (doseq [ratio (range ratio-min (+ ratio-max ratio-step) ratio-step)]
        (.write w (str ratio "," (find-bandwidth ratio options) "\n"))
        (.flush w)))))
