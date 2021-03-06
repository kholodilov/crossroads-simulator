(ns sumo-integration.core
  (:require [common.events  :as events]
            [common.service :as service]
            [common.crossroads :as crossroads]
            [sumo-integration.network :as network])
  (:import [it.polito.appeal.traci SumoTraciConnection]
           [de.tudresden.sumo.cmd Vehicle Simulation Lane Trafficlights ArealDetector]))

(defn simulation-time [conn]
  (.do_job_get conn (Simulation/getCurrentTime)))

(defn departed-vehicles-count [conn]
  (.do_job_get conn (Simulation/getDepartedNumber)))

(defn vehicles [conn]
  (seq (.do_job_get conn (Vehicle/getIDList))))

(defn vehicles-count
  ([conn]
    (.do_job_get conn (Vehicle/getIDCount)))
  ([conn lane-id]
    (.do_job_get conn (Lane/getLastStepVehicleNumber lane-id))))

(defn lane-occupancy [conn lane-id]
  (.do_job_get conn (Lane/getLastStepOccupancy lane-id)))

(defn lane-e2-queue-size [conn lane-e2-id]
  (.do_job_get conn (ArealDetector/getLastStepVehicleNumber lane-e2-id)))

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

(def DEPART_TRIGGERED -1)
(def DEPART_POS_FREE -3)
(def DEPART_POS_BASE -4)
(def DEPART_LANE_RANDOM -2)

(defn add-vehicle [conn crossroads-direction]
  (let [t (simulation-time conn)
        route (network/route-id crossroads-direction)
        id (str "v" t "_" route "_" (rand-int 10000000))
        speed 13.8]
    (.do_job_set conn (Vehicle/add id "car" route DEPART_TRIGGERED DEPART_POS_FREE speed DEPART_LANE_RANDOM))))

(defn report-tls [conn width height]
  (clojure.string/join ", "
    (for [x (crossroads/coord-range width)
          y (crossroads/coord-range height)]
      (retrieve-tl conn (network/crossroads-id x y)))))

(defn report [conn width height]
  (println (str 
    "Vehicles: " (vehicles-count conn)
  ))
  (println (str
    "Traffic lights: " (report-tls conn width height)
  ))
)

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

(defn report-queues [event-service conn width height]
  (doseq [x (crossroads/coord-range width)
          y (crossroads/coord-range height)
          crossroads-direction (crossroads/list-directions x y)]
    (let [queue (lane-e2-queue-size conn (network/lane-e2-id crossroads-direction width height))
          queue-event (events/queue-event crossroads-direction :queue queue)]
      (events/trigger-event event-service events/QueueEvent queue-event))))

(defn sumo-step-fn [event-service conn width height]
  (fn [_]
    (.do_timestep conn)
    ;(report conn width height)
    (report-queues event-service conn width height)
    (events/trigger-event event-service events/TotalVehiclesCountEvent {:count (vehicles-count conn)})
    (events/trigger-event event-service events/DepartedVehiclesCountEvent {:count (departed-vehicles-count conn)})))

(defn switch-lights-fn [conn]
  (fn [switch-events]
    (doseq [switch-event switch-events]
      (let [state ({"ns" "GGgrrrGGgrrr", "we" "rrrGGgrrrGGg"} (:direction switch-event))
            phase-time (:phase-time switch-event)
            phase-length (:phase-length switch-event)
            duration (* (- phase-length phase-time) 1000)
            id (network/crossroads-id (:x switch-event) (:y switch-event))]
        (update-tl-state conn id state)
        (update-tl-remaining-duration conn id duration)))))

(defn add-vehicle-fn [conn]
  (fn [vehicle-events]
    (doseq [vehicle-event vehicle-events]
      (let [crossroads-direction vehicle-event] ; two things are equivalent now
        (add-vehicle conn vehicle-event)))))

(defn run-sumo [event-service simulation-cfg width height sumo-mode step-length]
  (let [sumo-conn (start-sumo "/opt/sumo" sumo-mode simulation-cfg step-length)
        timer-statement (events/create-statement event-service (str "select * from pattern[every timer:interval(" step-length "msec)]"))
        switchlights-statement (events/create-statement event-service "select * from SwitchEvent")
        incoming-vehicles-statement (events/create-statement event-service "select * from VehicleEvent")]
    (events/subscribe event-service timer-statement (sumo-step-fn event-service sumo-conn width height))
    (events/subscribe event-service switchlights-statement (switch-lights-fn sumo-conn))
    (events/subscribe event-service incoming-vehicles-statement (add-vehicle-fn sumo-conn))
    (service/build-service
      :conn sumo-conn
      :stop-fn (fn []
        (events/destroy-statement event-service incoming-vehicles-statement)
        (events/destroy-statement event-service switchlights-statement)
        (events/destroy-statement event-service timer-statement)
        (.close sumo-conn)))
  ))
