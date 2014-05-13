(ns client.core
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn display-counter [counter]
  (ef/at "#crossroads tr td" (ef/content counter)))

(def ws (js-obj))

(defn init-websocket [ws-url]
  (set! ws (js/WebSocket. ws-url))
  (set! (.-onopen ws) (fn [] (.log js/console "Connection opened...")))
  (set! (.-onclose ws) (fn [] (.log js/console "Connection closed...")))
  (set! (.-onmessage ws) (fn [message] (display-counter (.-data message)))))

(defn setup-crossroads-table []
  (GET "/size" {:handler
    (fn [{:keys [width height]}]
      (ef/at "#crossroads tr td"
        (em/clone-for [i (range width)]))
      (ef/at "#crossroads tr"
        (em/clone-for [i (range height)])))
    }))

(defn start []
  (setup-crossroads-table))
  ;(init-websocket "ws://localhost:3000/counter-ws"))

(set! (.-onload js/window) #(em/wait-for-load (start)))

