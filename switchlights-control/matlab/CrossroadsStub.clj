(import CrossroadsStub.CrossroadsStub)
(println "Hello")
(def cs (CrossroadsStub. ))

(def calcModelArgs (object-array ["a" "b" "c"]))
(def calcModelResult (vec (.calculateCrossroadModel cs 4 calcModelArgs)))
(println calcModelResult)
(let [[mw_mfParams mw_mfCounts mw_fRules mw_modelParams] calcModelResult
      mfParams (.getInt mw_mfParams)
      mfCounts (.getInt mw_mfCounts)
      fRules (.getInt mw_fRules)
      modelParams (.getInt mw_modelParams)]
     (println mfParams " " mfCounts " " fRules " " modelParams)
)
(doseq [mw calcModelResult] (.dispose mw))

(def getCycleTimeArgs
  (object-array [
    (object-array [4 3 2 1])
    10
    20
    (object-array [1 0 1 0])
    1
    2
    3
    4
    "test"]))
(def getCycleTimeResult (vec (.getCycleTime cs 1 getCycleTimeArgs)))
(println getCycleTimeResult)
(let [[mw_cycleTime] getCycleTimeResult
      cycleTime (.getInt mw_cycleTime)]
     (println "cycleTime: " cycleTime)
)
(doseq [mw getCycleTimeResult] (.dispose mw))

(.dispose cs)