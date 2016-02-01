(ns arpad.pool.manager
  (:require [clojure.core.async :as async :refer [<! >! close! go-loop]]
            [arpad.pool                   :refer [lookup-player
                                                  follow-player
                                                  ignore-player
                                                  update-pool
                                                  settings
                                                  standings]]))

(defn- get-sub-pool
  [pool sub-pool-name]
  (if-let [sub-pool (get pool sub-pool-name)]
    sub-pool
    (merge {:players {}} (settings pool))))

(defn mutate-pool
  "Generate a new pool, depending on the contents of the message"
  [pool msg]
  (case (:cmd msg)
    :new-game
    (let [[player-a player-b score] (:score msg)]
      (-> pool
          (update-pool player-a player-b score)
          (update-in [:players (:id player-a) :total-games] inc)
          (update-in [:players (:id player-b) :total-games] inc)))

    :follow
    (follow-player pool (:player msg))

    :ignore
    (ignore-player pool (:player msg))

    pool))

(defn gen-report
  "Generate a report based on the contents of the message"
  [pool msg]
  (let [report
        (case (:cmd msg)
          :new-game
          (let [[player-a player-b _] (:score msg)]
            {:players (lookup-player pool player-a player-b)})

          :follow
          {:following (:player msg)}

          :ignore
          {:ignoring (:player msg)}

          :rating
          {:players (lookup-player pool (:player msg))}

          :standings
          (if-let [n (:number msg)]
            {:standings (standings pool n)}
            {:standings (standings pool)})

          {:error msg})]
    (let [pool (or (:pool msg) :default)]
      (assoc report :pool pool))))

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
     (= (:cmd msg) :undo)
     (if last-state
       (do (>! (:new-state out-chans) last-state)
           (>! (:player-report out-chans) {:undo 1})
           (recur (<! in-chan) last-state nil))
       (do (>! (:player-report out-chans) {:error :cannot-undo})
           (recur (<! in-chan) pool nil)))

     msg
     (let [sub-pool-name (or (:pool msg) :default)
           sub-pool (get-sub-pool pool sub-pool-name)
           sub-pool' (mutate-pool sub-pool msg)
           pool' (assoc pool sub-pool-name sub-pool')
           report (gen-report sub-pool' msg)
           undo-state (if (= sub-pool sub-pool') last-state pool)]
       (>! (:new-state out-chans) pool')
       (>! (:player-report out-chans) report)
       (recur (<! in-chan) pool' undo-state))

     close?
     (doseq [c (vals out-chans)]
       (close! c))

     :else nil)))
