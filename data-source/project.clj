(defproject kholodilov.crossroads-simulator/data-source "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure _]
                 [ruiyun/tools.timer "1.0.1"]
                 [com.novemberain/langohr _]
                 [kholodilov.crossroads-simulator/common _]]
  :plugins [[lein-modules "0.3.6"]]
  :test-selectors {
    :default (fn [m] (not (:integration m)))
    :itest (fn [m] true)
  }
  :main data-source.core)
