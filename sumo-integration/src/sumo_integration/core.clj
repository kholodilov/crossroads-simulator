(ns sumo-integration.core
  (:import [it.polito.appeal.traci SumoTraciConnection]
           [de.tudresden.sumo.cmd Vehicle]))

(defn -main [& args]
  (let [step 300
        step-length-seconds (str (/ step 1000.))
        conn (doto 
                (SumoTraciConnection. "/opt/sumo/bin/sumo-gui" "simulation/config.sumo.cfg")
                (.addOption "step-length" step-length-seconds)
                (.addOption "start" nil))]
    (.runServer conn)
    (doseq [i (range 1 300)]
      (do
        (.do_timestep conn)
        (.do_job_set conn (Vehicle/add (str "v" i) "car" "r1" (* i step) 0 13.8 1))
        (println i)
        (Thread/sleep step)
      ))
    (println "close")
    (.close conn)
  ))
