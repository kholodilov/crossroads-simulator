(ns client.core)

(defn init-websocket [ws-url initializer handler]
  (let [ws (js/WebSocket. ws-url)]
    (set! (.-onopen ws)
      (fn []
        (.log js/console "Connection opened...")
        (initializer ws)
        (.log js/console "Websocket initialized...")))
    (set! (.-onclose ws) (fn [] (.log js/console "Connection closed...")))
    (set! (.-onmessage ws) handler)
    ws))
