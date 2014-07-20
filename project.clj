(defproject crossroads-simulator "0.1.0-SNAPSHOT"
  :description "Crossroads simulator"
  :url "https://github.com/kholodilov/crossroads-simulator"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-modules "0.3.6"]]
  :modules {
    :subprocess false
    :versions {
      clojure "1.6.0"
      tools.cli "0.3.1"
      langohr "2.11.0"
    }
  }
)

