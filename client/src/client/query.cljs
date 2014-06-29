(ns client.query
  (:require [client.core :refer [init-websocket]]
            [enfocus.core :as ef]
            [jayq.core :as jayq])
  (:require-macros [enfocus.macros :as em]))

(defn display-query-result [query-result]
  (ef/at ".query-results"
    (ef/prepend (str "<pre>" query-result "</pre>"))))

(defn StartQueryEngine []
  (em/wait-for-load
    (init-websocket "ws://localhost:3000/events" #() #(display-query-result (.-data %)))))
