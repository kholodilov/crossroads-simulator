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
      getPhaseLengthArgs
        (object-array [
          (double-array [40 3 20 1])
          20.0
          (double-array [1 0 1 0])
          mw_mfParams
          mw_mfCounts
          mw_fRules
          mw_modelParams])
      getPhaseLengthResult
        (vec (.getPhaseLength cc 1 getPhaseLengthArgs))
     [mw_phaseLength] getPhaseLengthResult
     phaseLength (.getDouble mw_phaseLength)]

    (println "phaseLength: " phaseLength)
    (doseq [mw getPhaseLengthResult] (.dispose mw)))

(doseq [mw calcModelResult] (.dispose mw))

(.dispose cc)