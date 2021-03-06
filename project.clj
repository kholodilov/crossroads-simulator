(defproject kholodilov.crossroads-simulator/crossroads-simulator "0.1.0-SNAPSHOT"
  :description "Crossroads simulator"
  :url "https://github.com/kholodilov/crossroads-simulator"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-modules "0.3.6"]]
  :profiles {
    :test {
      :dependencies [[pjstadig/humane-test-output "0.7.0"]]
      :injections   [(require 'pjstadig.humane-test-output)
                     (pjstadig.humane-test-output/activate!)]
      :test-selectors ^:replace {
        :default (fn [m] (not (:integration m)))
        :itest (fn [m] true)
      }
    }
  }
  :modules {
    :subprocess false
    :versions {
      clojure "1.6.0"
      org.clojure/core.match "0.2.2"
      ruiyun/tools.timer "1.0.1"
      kholodilov.crossroads-simulator :version
    }
  }
)

