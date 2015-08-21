(ns arpad.pool.load
  (:require [arpad.pool.schema :refer [json->Pool]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(defn- exists?
  "Returns 'true' if 'filename' is a readable file."
  [filename]
  (.exists (io/file filename)))

(defn load-pool
  "Loads a pool, given a file containing the pool in JSON
  format. If :throw? is false, return nil if the file does not
  exist (default: true)."
  [filename & {:keys [throw?] :or {throw? true}}]
  (when (or throw? (exists? filename))
    (-> (slurp filename)
        (json/read-str :key-fn keyword)
        (json->Pool))))
