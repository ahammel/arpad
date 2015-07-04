(ns arpad.k)

(defn  uscf-k
  "The K-value computation used by the United States Chess Federation"
  [player]
  {:pre [(contains? player :rating)]}
  (let [rating (:rating player)]
    (cond
     (< rating 2100) 32
     (< rating 2400) 24
     :else           16)))

(defn  fide-k
  "The K-value computation used by the Fédération Internationale des Échecs"
  [player]
  {:pre [(contains? player :total-games)
         (contains? player :peak-rating)]}
  (cond
   (< (:total-games player) 30)   30
   (< (:peak-rating player) 2400) 15
   :else                          10))

(def k-functions
  {:uscf uscf-k
   :fide fide-k})
