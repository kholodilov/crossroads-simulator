(import CrossroadControl.CrossroadControl)
(import com.mathworks.toolbox.javabuilder.MWNumericArray)
(import com.mathworks.toolbox.javabuilder.MWClassID)

(println "Hello")
(def cc (CrossroadControl. ))

(def calcModelArgs
  (object-array [
    "../../model.RLSM.error.0.12.date.2013-10-08.mat"
    ""
    "RLSM"]))
(def calcModelResult (vec (.calculateCrossroadModel cc 4 calcModelArgs)))

(let [[mw_mfParams mw_mfCounts mw_fRules mw_modelParams] calcModelResult
      getCycleTimeArgs
        (object-array [
          (double-array [4 3 2 1])
          40.0
          10.0
          (double-array [1 0 1 0])
          mw_mfParams
          mw_mfCounts
          mw_fRules
          mw_modelParams
          "fminbnd"])
      getCycleTimeResult
        (vec (.getCycleTime cc 1 getCycleTimeArgs))
     [mw_cycleTime] getCycleTimeResult
     cycleTime (.getInt mw_cycleTime)]

    (println "cycleTime: " cycleTime)
    (doseq [mw getCycleTimeResult] (.dispose mw)))

(doseq [mw calcModelResult] (.dispose mw))

(.dispose cc)