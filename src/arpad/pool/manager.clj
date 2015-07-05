(ns arpad.pool.manager
  (:require [clojure.core.async :as async :refer [<! >! go]]
            [clojure.core.match           :refer [match]]
            [arpad.pool                   :refer [lookup-player
                                                  update-pool
                                                  standings]]))

(defn make-output-channels []
  {:player-report (async/chan)})

(defn respond
  "Respond to a message"
  [msg pool-agent out-chans]
  (match [msg]
    [{:new-game [player-a player-b score]}]
    (do
      (send pool-agent
            #(-> %
                 (update-pool player-a player-b score)
                 (update-in [:players (:id player-a) :total-games] inc)
                 (update-in [:players (:id player-b) :total-games] inc)))
      (go (when (await-for 10000 pool-agent)
            (>! (:player-report out-chans)
                (lookup-player @pool-agent player-a player-b)))))

    [{:standings n}]
    (go (>! (:player-report out-chans)
            (if n
              (standings @pool-agent n)
              (standings @pool-agent))))

    :else
    (println "no match")             ; TODO: log an error or something
    ))

(defn spawn-pool-manager
  [pool-agent in-chan]
  (let [out-chans (make-output-channels)]
    (go (while true
          (respond (<! in-chan) pool-agent out-chans)))
    out-chans))
