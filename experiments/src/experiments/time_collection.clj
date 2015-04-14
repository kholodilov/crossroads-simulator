(ns experiments.time-collection
  (:require [experiments.core]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clj-time.local :as time-local]
            [clj-time.format :as time-format]
            [common.events      :as events]
            [common.service     :as service]
            [common.timer       :as timer]
            [sumo-integration.core :as sumo]
            [sumo-integration.generator :as sumo-generator]))

(def experiment-name (experiments.core/experiment-name "time-collection"))

(defn run-simulation [q1 q2 q3 q4 ph1 ph2 speed sumo-mode]
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
                        :tls [{:id "0/0" :program-id "1" :phases [{:duration ph1 :state "rrrGGgrrrGGg"} {:duration ph2 :state "GGgrrrGGgrrr"}]}
                              {:id "0/1" :program-id "off"}
                              {:id "1/0" :program-id "off"}
                              {:id "1/1" :program-id "off"}])
        sumo-service (sumo/run-sumo event-service simulation-cfg width height sumo-mode 1000)
        timer-service (timer/run-timer event-service 1000 speed)

        queues-statement (events/create-statement event-service "select sum(q.queue) q, current_timestamp t from QueueEvent.std:unique(x, y, direction) as q where q.x = 0 and q.y = 0")

        stop-fn #(do
                  (service/stop timer-service)
                  (service/stop sumo-service)
                  (service/stop event-service))]

    (events/subscribe event-service queues-statement
      (fn [[event & _]]
        ;(println event)
        (if (and (= 0 (:q event)) (>= (:t event) 7000))
          (do
            ;(println (str "STOP - time " (:t event)))
            (deliver T (:t event))))))

    (let [Tmillis (deref T 10000 -1000)
          Tseconds (quot Tmillis 1000)]
      (stop-fn)
      Tseconds)
  ))

(def cli-options
  [["-n" "--iterations n" "Count of iterations"
    :default 100
    :parse-fn #(Integer/parseInt %)]
   ["-q" "--max-q n" "Maximum queue size"
    :default 45
    :parse-fn #(Integer/parseInt %)]
   ["-p" "--max-phase n" "Maximum phase length"
    :default 30
    :parse-fn #(Integer/parseInt %)]
   ["-o" "--output file" "Output file name"
    :default (str experiment-name ".csv")]
   ["-s" "--speed n" "Speed-up coefficient (10 -> 10x speed-up)"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-m" "--sumo-mode mode" "Simulation mode - 'cli' or 'gui'"
    :default :cli
    :parse-fn #(keyword %)]])

(defn iteration [max-q max-phase speed sumo-mode]
  (let [qs (repeatedly 4 #(rand-int max-q))
        phs (repeatedly 2 #(+ 1 (rand-int max-phase)))
        params (vec (flatten [qs phs]))]
    (println (str "Params: " params))
    (let [T (apply run-simulation (concat params [speed sumo-mode]))]
      (clojure.string/join "," (conj params T)))))

(defn -main [& args]
  (let [{:keys [max-q max-phase iterations output speed sumo-mode]}
          (:options (cli/parse-opts args cli-options))]
    (with-open [w (io/writer output)]
      (doseq [i (range iterations)]
        (println (str "Iteration " i))
        (.write w (iteration max-q max-phase speed sumo-mode))
        (.write w "\n")
        (.flush w)))))
