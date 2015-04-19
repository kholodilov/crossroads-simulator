(ns common.service)

(defprotocol Stoppable
  (stop [this]))

(defrecord Service [conn stop-fn]
  Stoppable
    (stop [this]
      ((:stop-fn this))))

(defn build-service [& {:keys [conn stop-fn]}]
  (Service. conn stop-fn))

(defn noop-service []
  (build-service :stop-fn #()))
