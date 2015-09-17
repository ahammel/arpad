(ns arpad.pretty-printer.english
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.pprint :refer [cl-format]]
            [clojure.string :as string]))

(defn- id->str
  "Convert a player id (keyword) to a string"
  [id]
  (string/replace-first (str id) ":" ""))

(defn- backticks
  "Wrap a string in ```triple backticks```"
  [string]
  (str "```" string "```"))

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
  (let [format-str "~2d. @~a:~40,0t~4d"
        ;;; ^^^ Warning: deep Common Lisp magic.
        ;;;
        ;;; You can look up the cl-format docs yourself, but the
        ;;; interesting part is '~40,0t', which aligns the ratings to
        ;;; the 40th column,which I figure is a reasonable deafult for
        ;;; now. There's probably a way to dynamically calculate this
        ;;; based on the longest name in the map.
        print-row  (fn [[player {rating :rating}] ordinality]
                     (cl-format nil
                                format-str
                                (inc ordinality)
                                (id->str player)
                                (int rating)))]
    (if (empty? standings) "OK"
        (backticks (string/join "\n" (map print-row standings (range)))))))

(defn- print-players
  "Print a ratings message for a player."
  [players]
  {:pre [(every? #(contains? (second %) :rating) players)
         (every? #(contains? (second %) :peak-rating) players)
         (every? #(contains? (second %) :total-games) players)]}
  (let [format-str "@~a ~20,0t Rating: ~4d  Games played: ~4d  Peak rating: ~4d"
        print-row (fn [[player stats]]
                    (cl-format nil
                               format-str
                               (id->str player)
                               (int (:rating stats))
                               (:total-games stats)
                               (int (max (:rating stats)
                                         (:peak-rating stats)))))]
    (if (empty? players) "OK"
        (backticks (string/join "\n" (map print-row players))))))

(defn pretty-print
  "Convert a player report to human-readable text."
  [report]
  (match [report]
    [{:following {:id player}}]
    (str "Following @" (id->str player))

    [{:ignoring {:id player}}]
    (str "Ignoring @" (id->str player))

    [{:standings standings}]
    (print-standings standings)

    [{:players players}]
    (print-players players)

    :else
    (str "ERR: Cannot pretty-print the message '" (str report) "'")))

(defn spawn-pretty-printer
  "Creates a process which takes player-reports from the in-chan and
  supplies human-readabletext strings to the out-chan"
  [in-chan out-chan]
  (async/pipeline 1 out-chan (map pretty-print) in-chan))
