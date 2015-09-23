(ns arpad.pool.manager
  (:require [clojure.core.async :as async :refer [<! >! close! go-loop]]
            [clojure.core.match           :refer [match]]
            [arpad.pool                   :refer [lookup-player
                                                  follow-player
                                                  ignore-player
                                                  update-pool
                                                  standings]]))

(defn mutate-pool
  "Generate a new pool, depending on the contents of the message"
  [pool msg]
  (match [msg]
    [{:new-game [player-a player-b score]}]
    (-> pool
        (update-pool player-a player-b score)
        (update-in [:players (:id player-a) :total-games] inc)
        (update-in [:players (:id player-b) :total-games] inc))

    [{:follow player}]
    (follow-player pool player)

    [{:ignore player}]
    (ignore-player pool player)

    :else pool))

(defn gen-report
  "Generate a report based on the contents of the message"
  [pool msg]
  (match [msg]
    [{:new-game [player-a player-b _]}]
    {:players
     (lookup-player pool player-a player-b)}

    [{:follow player}]
    {:following player}

    [{:ignore player}]
    {:ignoring player}

    [{:rating player}]
    {:players (lookup-player pool player)}

    [{:standings n}]
    (if n
      {:standings (standings pool n)}
      {:standings (standings pool)})

    :else
    {:error msg}))

(defn spawn-pool-manager
  "Spawn a process which manages the state of a pool of
  players. Commands are read from the 'in-chan' and applied to the
  pool. If the command changes the state of the pool, the new state is
  written to the :new-state chan. In any case, a report of the results
  of the command are sent to the :player-report chan.

  If :close? is true (default false), the process closes the child
  channels after then in-chan is closed."
  [init-pool in-chan out-chans &
   {:keys [close?] :or {close? false}}]
  {:pre [(contains? out-chans :player-report)
         (contains? out-chans :new-state)]}
  (go-loop [msg (<! in-chan)
            pool init-pool
            last-state nil]
    (cond
     (:undo msg)
     (if last-state
       (do (>! (:new-state out-chans) last-state)
           (>! (:player-report out-chans) {:undo 1})
           (recur (<! in-chan) last-state nil))
       (do (>! (:player-report out-chans) {:error :cannot-undo})
           (recur (<! in-chan) pool nil)))

     msg
     (let [pool' (mutate-pool pool msg)
           report (gen-report pool' msg)
           undo-state (if (= pool pool') last-state pool)]
       (>! (:new-state out-chans) pool')
       (>! (:player-report out-chans) report)
       (recur (<! in-chan) pool' undo-state))

     close?
     (doseq [c (vals out-chans)]
       (close! c))

     :else nil)))
