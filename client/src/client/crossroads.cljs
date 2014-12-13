(ns client.crossroads
  (:require [client.core :refer [init-websocket]]
            [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn display-switch-crossroads [x y phase-time cycle-time direction]
  (ef/at
    (str ".crossroads .crow:nth-child(" y ") .ccol:nth-child(" x ")")
    (ef/do-> 
      (ef/content (str (- cycle-time phase-time)))
      (ef/set-class "ccol" direction))
  ))

(defn crossroads-event-handler [message]
  (let [data (cljs.reader/read-string (.-data message))
        {:keys [x y phase-time cycle-time direction]} data]
    (display-switch-crossroads x y phase-time cycle-time direction)))

(defn setup-crossroads-table []
  (GET "/state" {:handler
    (fn [{:keys [width height switch-times]}]
      (ef/at ".crossroads .crow .ccol"
        (em/clone-for [i (range width)]))
      (ef/at ".crossroads .crow"
        (em/clone-for [i (range height)]))
      (doseq [{:keys [x y phase-time cycle-time direction]} switch-times]
        (display-switch-crossroads x y phase-time cycle-time direction))
    )}))

(defn StartCrossroadsSimulator []
  (em/wait-for-load
    (setup-crossroads-table)
    (init-websocket "ws://localhost:3000/query"
      #(.send % "select * from SwitchEvent")
      crossroads-event-handler)))

