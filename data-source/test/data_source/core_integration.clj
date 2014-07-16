(ns data-source.core-integration
  (:require [clojure.test :refer :all]
            [data-source.core  :as ds]
            [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc])
  (:use [clojure.java.shell :only (sh)]))

(defn handler [atom]
  (fn [ch _ ^bytes payload] (reset! atom (String. payload "UTF-8"))))

(deftest ^:integration test-switch-events-source
  (sh "rabbitmq-server" "-detached")
  (Thread/sleep 4000)
  (let [queue "test-switch-events-source"
        result (atom nil)
        conn (rmq/connect)
        ch (lch/open conn)]
    (lq/declare ch queue)
    (lc/subscribe ch queue (handler result))
    (ds/-main queue)
    (is (= @result "Hello!")))
  (sh "rabbitmqctl" "stop"))
