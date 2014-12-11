(import CrossroadControl.CrossroadControl)
(println "Hello")
(def cc (CrossroadControl. ))

(def calcModelArgs
  (object-array [
    "model.RLSM.error.0.12.date.2013-10-08.mat"
    "train_data_output_cnt_simple.csv"
    "RLSM"]))
(def calcModelResult (vec (.calculateCrossroadModel cc 4 calcModelArgs)))

(let [[mw_mfParams mw_mfCounts mw_fRules mw_modelParams] calcModelResult
      mfParams (.getData mw_mfParams)
      mfCounts (.getData mw_mfCounts)
      fRules (.getData mw_fRules)
      modelParams (.getData mw_modelParams)]

  (println mfParams "\n" mfCounts "\n" fRules "\n" modelParams)

  (let [getCycleTimeArgs
        (object-array [
          (double-array [4 3 2 1])
          40.0
          10.0
          (double-array [1 0 1 0])
          mfParams
          mfCounts
          fRules
          modelParams
          "GA"])
        getCycleTimeResult
          (vec (.getCycleTime cc 1 getCycleTimeArgs))
       [mw_cycleTime] getCycleTimeResult
       cycleTime (.getInt mw_cycleTime)]

    (println "cycleTime: " cycleTime)
    (doseq [mw getCycleTimeResult] (.dispose mw))))

(doseq [mw calcModelResult] (.dispose mw))

(.dispose cc)