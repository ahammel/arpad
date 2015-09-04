(ns arpad.pretty-printer.english
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.pprint :refer [cl-format]]
            [clojure.string :as string]))

(defn- id->str
  "Convert a player id (keyword) to a string"
  [id]
  (string/replace-first (str id) ":" ""))

(defn- standings?
  "Returns nil if the argument does not match the standings message
  format"
  [s]
  (and
   (every? (comp map? second) s)
   (every? #(contains? (second %) :rating) s)))

(defn- print-standings
  "Pretty print a standings message"
  [standings]
  (let [format-str "@~a:~40,0t~4d"
        ;;; ^^^ Warning: deep Common Lisp magic.
        ;;;
        ;;; You can look up the cl-format docs yourself, but the
        ;;; interesting part is '~20,0t', which aligns the ratings to
        ;;; the 20th column,which I figure is a reasonable deafult for
        ;;; now. There's probably a way to dynamically calculate this
        ;;; based on the longest name in the map, but this is fine for
        ;;; now.
        print-row  (fn [[player {rating :rating}]]
                     (cl-format nil
                                format-str
                                (id->str player)
                                (int rating)))]
    (string/join "\n" (map print-row standings))))

(defn pretty-print
  "Convert a player report to human-readable text."
  [report]
  (match [report]
    [{:following {:id player}}]
    (str "Following @" (id->str player))

    [{:ignoring {:id player}}]
    (str "Ignoring @" (id->str player))

    :else
    (cond
     (empty? report) "OK"

     (standings? report)
     (print-standings report)

     :else
     (str "I didn't understand the message '" (str report) "'"))))

(defn spawn-pretty-printer
  "Creates a process which takes player-reports from the in-chan and
  supplies human-readabletext strings to the out-chan"
  [in-chan out-chan]
  (async/pipeline 1 out-chan (map pretty-print) in-chan))
