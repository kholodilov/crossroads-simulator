(ns sumo-integration.core
  (:require [ruiyun.tools.timer :as timer])
  (:import [it.polito.appeal.traci SumoTraciConnection]
           [de.tudresden.sumo.cmd Vehicle Simulation Lane]))

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

(defn add-vehicle [conn]
  (let [t (simulation-time conn)
        route (str "s" (rand-int 10))
        id (str "v" t "_" (rand-int 100))]
    (.do_job_set conn (Vehicle/add id "car" route t 0 13.8 0))
  ))

(defn format-percentage [fraction]
  (str (format "%.0f" (* 100 fraction)) "%"))

(defn report-lane [conn lane-id]
  (str lane-id ": " (vehicles-count conn lane-id) " / " (format-percentage (lane-occupancy conn lane-id))))

(defn report [conn]
  (println (str 
    "Vehicles: " (vehicles-count conn)
    ;", " (report-lane conn "0/0to0/1_0")
    ;", " (report-lane conn "0/1to0/2_0")
    ;", " (report-lane conn "0/1to1/1_0")
  )))

(defn -main [& args]
  (let [step-length 300
        step-length-seconds (str (/ step-length 1000.))
        conn (doto 
                (SumoTraciConnection. "/opt/sumo/bin/sumo-gui" "simulation_grid/config.sumo.cfg")
                (.addOption "step-length" step-length-seconds)
                (.addOption "start" nil))]
    (.runServer conn)
    (timer/run-task! #(.do_timestep conn) :period step-length)
    (timer/run-task! #(add-vehicle conn) :period (* step-length 2) :delay 1000)
    (timer/run-task! #(report conn) :period (* step-length 3))
  )
)
