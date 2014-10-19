(ns sumo-integration.core
  (:import [it.polito.appeal.traci SumoTraciConnection]
           [de.tudresden.sumo.cmd Vehicle Simulation]))

(defn simulation-time [conn]
  (.do_job_get conn (Simulation/getCurrentTime)))

(defn vehicles-in-simulation [conn]
  (seq (.do_job_get conn (Vehicle/getIDList))))

(defn -main [& args]
  (let [step-length 300
        step-length-seconds (str (/ step-length 1000.))
        conn (doto 
                (SumoTraciConnection. "/opt/sumo/bin/sumo-gui" "simulation/config.sumo.cfg")
                (.addOption "step-length" step-length-seconds)
                (.addOption "start" nil))]
    (.runServer conn)
    (doseq [i (range 1 3000)]
      (do
        (.do_timestep conn)
        (when (= 0 (rem i 5))
          (let [t (simulation-time conn)
                route (str "s" (+ 1 (rem i 2)))
                step (/ i 5)
                id (str "v" step)]
            (.do_job_set conn (Vehicle/add id "car" route t 0 13.8 0))
            (println (str "step: " step ", t: " t))
          )
          (println (str "Vehicles: " (vehicles-in-simulation conn)))
        )
        (Thread/sleep step-length)
      ))
    (println "close")
    (.close conn)
  ))
