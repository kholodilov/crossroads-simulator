(import SwitchLights.SwitchLights)
(println "Hello")
(def sl (SwitchLights. ))
(def args (object-array ["ns" 10 0 0 0 0]))
(def result (vec (.switchLights sl 2 args)))
(println result)
(let [[mw_state mw_duration] result
      state (apply str (.getData mw_state))
      duration (.getInt mw_duration)]
     (println state)
     (println duration)
)
(doseq [mw result] (.dispose mw))
(.dispose sl)
