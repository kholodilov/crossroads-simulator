(ns experiments.core
  (:require [clj-time.local :as time-local]
            [clj-time.format :as time-format]))

(defn experiment-name [name]
  (str name "-" (time-format/unparse (time-format/formatter "yyyy-MM-dd-HH-mm-ss") (time-local/local-now))))
