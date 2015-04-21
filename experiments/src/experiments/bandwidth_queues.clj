(ns experiments.bandwidth-queues
  (:require [experiments.core]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [common.events      :as events]
            [common.service     :as service]
            [common.timer       :as timer]
            [sumo-integration.core :as sumo]
            [sumo-integration.generator :as sumo-generator]
            [switchlights-control.core :as switchlights]))

(def experiment-name (experiments.core/experiment-name "bandwidth-queues"))

(defn run-simulation [q1 q2 q3 q4 {:keys [tls-adaptive phase-length speed sumo-mode]}]
  (let [T (promise)
        width 2
        height 2
        event-service (events/build-esper-service experiment-name)
        simulation-cfg (sumo-generator/generate-network "/opt/sumo" "/tmp" experiment-name
                        :width width :height height :grid-length 300 :attach-length 296 :e2-length 290
                        :routes [{:id "r0/0_1" :edges "left0to0/0 0/0to1/0"}
                                 {:id "r0/0_2" :edges "0/1to0/0 0/0tobottom0"}
                                 {:id "r0/0_3" :edges "1/0to0/0 0/0toleft0"}
                                 {:id "r0/0_4" :edges "bottom0to0/0 0/0to0/1"}]
                        :vehicles-defs [{:route-id "r0/0_1" :count q1}
                                        {:route-id "r0/0_2" :count q2}
                                        {:route-id "r0/0_3" :count q3}
                                        {:route-id "r0/0_4" :count q4}]
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
                       :phase-length-update-frequency nil
                       :initial-switch-events [
                        {:x 0 :y 0 :phase-time (dec phase-length) :phase-length phase-length :direction "ns"}
                       ]
                       :initial-queues [
                        {:x 0 :y 0 :direction 1 :queue q1}
                        {:x 0 :y 0 :direction 2 :queue q2}
                        {:x 0 :y 0 :direction 3 :queue q3}
                        {:x 0 :y 0 :direction 4 :queue q4}
                      ]}
        switchlights-service (if tls-adaptive
                               (switchlights/run-switchlights event-service 1 1 phase-length switchlights-params)
                               (service/noop-service))

        timer-service (timer/run-timer event-service 1000 speed)

        queues-statement (events/create-statement event-service "select sum(q.queue) q, current_timestamp t from QueueEvent.std:unique(x, y, direction) as q where q.x = 0 and q.y = 0")

        stop-fn #(do
                  (service/stop timer-service)
                  (service/stop switchlights-service)
                  (service/stop sumo-service)
                  (service/stop event-service))]

    (events/subscribe event-service queues-statement
      (fn [[event & _]]
        ;(println event)
        (if (and (= 0 (:q event)) (>= (:t event) 7000))
          (do
            ;(println (str "STOP - time " (:t event)))
            (deliver T (:t event))))))

    (let [Tseconds (quot (deref T) 1000)]
      (stop-fn)
      Tseconds)
  ))

(def cli-options
  [["-o" "--console-output" "Output only to console"]
   ;["-a" "--tls-adaptive" "Enable TLS adaptive mode"]
   ["-s" "--speed n" "Speed-up coefficient (10 -> 10x speed-up)"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-p" "--phase-length p" "Length of traffic light phase in fixed phases mode, maximum length of phase in adaptive mode"
    :default 30
    :parse-fn #(Integer/parseInt %)]
   [nil "--q1-min n" ""
    :default 0
    :parse-fn #(Integer/parseInt %)]
   [nil "--q1-max n" ""
    :default 30
    :parse-fn #(Integer/parseInt %)]
   [nil "--q2-min n" ""
    :default 0
    :parse-fn #(Integer/parseInt %)]
   [nil "--q2-max n" ""
    :default 30
    :parse-fn #(Integer/parseInt %)]
   ["-m" "--sumo-mode mode" "Simulation mode - 'cli' or 'gui'"
    :default :cli
    :parse-fn #(keyword %)]])

(defn -main [& args]
  (let [{:keys [console-output q1-min q1-max q2-min q2-max] :as options}
          (:options (cli/parse-opts args cli-options))
        output (if console-output
                  System/out
                  (str experiment-name ".csv"))]
    (with-open [w (io/writer output)]
      (doseq [q1 (range q1-min (inc q1-max)) q2 (range q2-min (inc q2-max))]
        (let [Tstatic (run-simulation q1 q2 q1 q2 (merge options {:tls-adaptive false}))
              Tadaptive (run-simulation q1 q2 q1 q2 (merge options {:tls-adaptive true}))]
          (.write w (str q1 "," q2 "," Tstatic "," Tadaptive "\n"))
          (.flush w))))))
