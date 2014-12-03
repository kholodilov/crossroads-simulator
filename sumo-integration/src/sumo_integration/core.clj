(ns sumo-integration.core
  (:require [ruiyun.tools.timer :as timer]
            [common.service :as service])
  (:import [it.polito.appeal.traci SumoTraciConnection]
           [de.tudresden.sumo.cmd Vehicle Simulation Lane Trafficlights]))

(defn simulation-time [conn]
  (.do_job_get conn (Simulation/getCurrentTime)))

(defn vehicles [conn]
  (seq (.do_job_get conn (Vehicle/getIDList))))

(defn vehicles-count
  ([conn]
    (.do_job_get conn (Vehicle/getIDCount)))
  ([conn lane-id]
    (.do_job_get conn (Lane/getLastStepVehicleNumber lane-id))))

(defn lane-occupancy [conn lane-id]
  (.do_job_get conn (Lane/getLastStepOccupancy lane-id)))

(defrecord TrafficLights [id phase-id phase-duration remaining-duration state]
  Object
  (toString [tl]
    (str (:id tl) "=" 
         (:remaining-duration tl) "/" (:phase-duration tl)
         "(" (:phase-id tl) ":" (:state tl) ")"
    )))

(defn build-tl [& {:keys [id phase-id phase-duration remaining-duration state]}]
  (TrafficLights. id phase-id phase-duration remaining-duration state))

(defn retrieve-tl [conn id]
  (build-tl
    :id id
    :phase-id
      (.do_job_get conn (Trafficlights/getPhase id))
    :phase-duration
      (.do_job_get conn (Trafficlights/getPhaseDuration id))
    :remaining-duration
      (- (.do_job_get conn (Trafficlights/getNextSwitch id))
        (simulation-time conn))
    :state
      (.do_job_get conn (Trafficlights/getRedYellowGreenState id))
  ))

(defn update-tl-remaining-duration [conn id duration]
  (.do_job_set conn (Trafficlights/setPhaseDuration id duration)))

(defn update-tl-state [conn id state]
  (.do_job_set conn (Trafficlights/setRedYellowGreenState id state)))

(defn add-vehicle [conn]
  (let [t (simulation-time conn)
        add-time (+ t 1000)
        route (str "s" (rand-int 10))
        id (str "v" add-time "_" (rand-int 100))]
    (.do_job_set conn (Vehicle/add id "car" route add-time 0 13.8 0))
  ))

(defn format-percentage [fraction]
  (str (format "%.0f" (* 100 fraction)) "%"))

(defn report-lane [conn lane-id]
  (str lane-id ": " (vehicles-count conn lane-id) " / " (format-percentage (lane-occupancy conn lane-id))))

(defn coord-range [dimension] (range 1 (+ dimension 1)))

(defn tl-id [x y] (str x "/" y))

(defn report-tls [conn width height]
  (clojure.string/join ", "
    (for [x (coord-range width)
          y (coord-range height)]
      (retrieve-tl conn (tl-id x y)))))

(defn report [conn width height]
  (println (str 
    "Vehicles: " (vehicles-count conn)
    ;", " (report-lane conn "0/0to0/1_0")
    ;", " (report-lane conn "0/1to0/2_0")
    ;", " (report-lane conn "0/1to1/1_0")
  ))
  (println (str
    "Traffic lights: " (report-tls conn width height)
  ))
)

(defn tl-monkey [conn width height]
  (let [x (rand-nth (coord-range width))
        y (rand-nth (coord-range height))
        id (tl-id x y)
        tl (retrieve-tl conn id)
        duration (:remaining-duration tl)
        new-duration (+ 3000 duration)]
    (update-tl-remaining-duration conn id new-duration)
    (println (str "### tl-monkey: " id " " duration "->" new-duration))))

(def tl-program 
  (flatten (repeat [{:state "rrrGGgrrrGGg" :duration 31000}
                    {:state "rrryyyrrryyy" :duration 4000}
                    {:state "GGgrrrGGgrrr" :duration 31000}
                    {:state "yyyrrryyyrrr" :duration 4000}])))

(defn switch-lights [conn timer width height program]
  (let [program-step (first program)
        state (:state program-step)
        duration (:duration program-step)
        program* (rest program)]
    (doseq [x (coord-range width)
            y (coord-range height)]
      (let [id (tl-id x y)]
        (update-tl-state conn id state)
        (update-tl-remaining-duration conn id duration)))
    (timer/run-task! #(switch-lights conn timer width height program*) :by timer :delay duration)))

(defn sumo-binary-path [sumo-home sumo-mode]
  (if-let [binary-name ({:gui "sumo-gui", :cli "sumo"} sumo-mode)]
    (str sumo-home "/bin/" binary-name)
    (throw (RuntimeException. (str "Unknown SUMO mode: " sumo-mode)))))

(defn start-sumo [sumo-home sumo-mode simulation-conf step-length]
  (let [step-length-seconds (str (/ step-length 1000.))
        binary-path (sumo-binary-path sumo-home sumo-mode)
        conn (doto 
                (SumoTraciConnection. binary-path simulation-conf)
                (.addOption "step-length" step-length-seconds)
                (.addOption "start" nil))]
    (.runServer conn)
    conn))

(defn run-sumo [sumo-home sumo-mode step-length]
  (let [width 3
        height 2
        sumo-conn (start-sumo sumo-home sumo-mode "simulation_grid/config.sumo.cfg" step-length)
        timer (timer/timer)]
    (timer/run-task! #(.do_timestep sumo-conn) :by timer :period step-length)
    (timer/run-task! #(add-vehicle sumo-conn) :by timer :period (* step-length 2) :delay 1000)
    (timer/run-task! #(report sumo-conn width height) :by timer :period (* step-length 2))
    (timer/run-task! #(tl-monkey sumo-conn width height) :by timer :period (* step-length 50))
    (switch-lights sumo-conn timer width height tl-program)
    (service/build-service
      :stop-fn (fn []
        (timer/cancel! timer)
        (.close sumo-conn)))
  ))

(defn -main [& args]
  (let [step-length 1000
        [sumo-home sumo-mode] args]
    (run-sumo sumo-home (keyword sumo-mode) step-length)))
