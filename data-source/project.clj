(defproject data-source "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure _]
                 [org.clojure/tools.cli _]
                 [ruiyun/tools.timer "1.0.1"]
                 [com.novemberain/langohr _]]
  :plugins [[lein-modules "0.3.6"]]
  :test-selectors {
    :default (fn [m] (not (:integration m)))
    :itest (fn [m] true)
  }
  :main data-source.core)
