(ns arpad.core
  (:gen-class)
  (:require [clojure.core.async :refer [>! <! <!! close! chan go
                                        sliding-buffer]]
            [clojure.java.io :as io]
            [arpad.commands.english :refer [str->Command]]
            [arpad.pool.load :refer [load-pool]]
            [arpad.pool.manager :refer [spawn-pool-manager]]
            [arpad.persistor :refer [run-persistor]]
            [arpad.pretty-printer.english :refer [spawn-pretty-printer]]))

(def filename "pool")

(defn- spawn-input-reader
  "Spawn a process which parses commands from a file called 'commands'
  and sends them to a channel"
  [out-chan]
  (go
    (with-open [rdr (io/reader "commands")]
      (doseq [line (line-seq rdr)]
        (>! out-chan (str->Command line))))
    (close! out-chan)))

(defn- run-printer
  [in-chan]
  (loop [msg (<!! in-chan)]
    (when msg
      (println msg)
      (flush)
      (recur (<!! in-chan)))))

(defn -main
  [& args]
  (let [init-pool (or (load-pool filename :throw? false)
                      {:players {}
                       :k :uscf
                       :default-rating 750})
        command-chan (chan)
        player-report (chan)
        new-state (chan (sliding-buffer 1))
        stdout-chan (chan)]
    (spawn-input-reader command-chan)
    (spawn-pool-manager init-pool
                        command-chan
                        {:new-state new-state :player-report player-report}
                        :close? true)
    (spawn-pretty-printer player-report stdout-chan)
    (run-printer stdout-chan)
    (run-persistor filename new-state)))
