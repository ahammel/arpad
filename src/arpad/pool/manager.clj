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
    (lookup-player pool player-a player-b)

    [{:follow player}]
    {:following player}

    [{:ignore player}]
    {:ignoring player}

    [{:standings n}]
    (if n (standings pool n) (standings pool))

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
            pool init-pool]
    (cond
     msg (let [pool' (mutate-pool pool msg)
               report (gen-report pool' msg)]
           (>! (:new-state out-chans) pool')
           (>! (:player-report out-chans) report)
           (recur (<! in-chan) pool'))
     close? (doseq [c (vals out-chans)]
              (close! c))
     :else nil)))
