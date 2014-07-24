(ns client.query
  (:require [client.core :refer [init-websocket]]
            [enfocus.core :as ef]
            [enfocus.events :as ev])
  (:require-macros [enfocus.macros :as em]))

(defn display-query-result [query-result]
  (ef/at ".query-results"
    (ef/prepend (str "<pre>" query-result "</pre>"))))

(em/defaction setup [ws]
  ["#submit-query"] (ev/listen :click #(.send ws (ef/from "#query" (ef/read-form-input))))
  ["#clear-results"] (ev/listen :click #(ef/at ".query-results" (ef/content ""))))

(defn StartQueryEngine []
  (let [ws (init-websocket "ws://localhost:3000/query" #() 
              #(display-query-result (sort (cljs.reader/read-string (.-data %)))))]
    (setup ws)))
