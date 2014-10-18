(ns sumo-integration.core
  (:import [it.polito.appeal.traci SumoTraciConnection]
           [de.tudresden.sumo.cmd Vehicle]))

(defn -main [& args]
  (let [conn (doto 
                (SumoTraciConnection. "/opt/sumo/bin/sumo-gui" "simulation/config.sumo.cfg")
                (.addOption "step-length" "0.1")
                (.addOption "start" nil))]
    (.runServer conn)
    (doseq [i (range 3600)]
      (do
        (.do_timestep conn)
        (.do_job_set conn (Vehicle/add (str "v" i) "car" "r1" 0 0 13.8 1))
      ))
    (println "close")
    (.close conn)
  ))
