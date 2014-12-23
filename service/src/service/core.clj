(ns service.core
  (:require [ruiyun.tools.timer :as timer]
            [clojure.tools.cli :as cli]
            [common.events      :as events]
            [common.service     :as service]
            [service.web        :as web]
            [switchlights-control.core :as switchlights]
            [sumo-integration.core :as sumo]))

(defn run-timer [event-service period]
  (let [time (atom 0)
        timer (timer/timer)
        timer-fn
          (fn []
            (swap! time + period)
            (events/do-timestep event-service @time))]
      (timer/run-task! timer-fn :period period :by timer :delay period)
      (service/build-service :stop-fn #(timer/cancel! timer))
    ))

(defn run-simulation [simulation-cfg width height max-phase-length phase-update-frequency sumo-mode]
  (let [event-service (events/build-esper-service "CrossroadsSimulator")
        timer-service (run-timer event-service 100)
        sumo-service (sumo/run-sumo event-service simulation-cfg width height sumo-mode 1000)
        switchlights-service (switchlights/run-switchlights event-service width height max-phase-length phase-update-frequency)
        web-service (web/start-web-service event-service {:port 3000})]

    (service/build-service
      :stop-fn #(do
        (service/stop web-service)
        (service/stop switchlights-service)
        (service/stop sumo-service)
        (service/stop timer-service)
        (service/stop event-service)))))

(def cli-options
  [["-w" "--width n" "Width"
    :default 3
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--height n" "Height"
    :default 2
    :parse-fn #(Integer/parseInt %)]
   ["-l" "--max-phase-length n" "Max phase length"
    :default 40
    :parse-fn #(Integer/parseInt %)]
   ["-f" "--phase-update-frequency n" "Phase update frequency"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-s" "--simulation-cfg n" "Simulation config"
    :default "../simulation_grid/config.sumo.cfg"]])

(defn -main [& args]
  (let [{:keys [simulation-cfg width height max-phase-length phase-update-frequency]} (:options (cli/parse-opts args cli-options))]
    (run-simulation simulation-cfg width height max-phase-length phase-update-frequency :gui)))
