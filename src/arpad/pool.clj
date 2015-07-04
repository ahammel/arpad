(ns arpad.pool
  (:require [arpad.elo :refer [new-ratings]]))

(defn adjust-ratings
  [player-a player-b score k]
  (map #(assoc %1 :rating %2)
       [player-a player-b]
       (new-ratings player-a player-b score k)))

(defn new-player
  [defaults]
  (merge {:rating 0 :total-games 0 :peak-rating 0} defaults))

(defn init-player
  ([pool player]
   {:pre [(contains? pool   :players)
          (contains? pool   :default-rating)
          (contains? player :id)]}
   (let [noob (new-player {:rating (:default-rating pool)})]
     (if (contains? (:players pool) (:id player))
       pool
       (assoc-in pool [:players (:id player)] noob))))
  ([pool player & more]
   (reduce init-player (init-player pool player) more)))

(defn update-pool
  "Update a pool given the results of a game (defaults to first player wins)"
  ([pool winner loser]
   (update-pool pool winner loser 1.0))
  ([pool winner loser score]
   {:pre [(contains? winner :id)
          (contains? loser :id)
          (contains? pool :players)
          (contains? pool :k)
          (<= score 1.0)
          (>= score 0.0)]}
   (let [pool' (init-player pool winner loser)
         player-a (get (:players pool') (:id winner))
         player-b (get (:players pool') (:id loser))
         [a' b']  (adjust-ratings player-a player-b score (:k pool))]
     (-> pool'
         (assoc-in [:players (:id winner)] a')
         (assoc-in [:players (:id loser)]  b')))))

(defn lookup-player
  "Return a player's information given an ID"
  [pool & players]
  {:pre [(contains? pool :players)
         (every? #(contains? % :id) players)]}
  (letfn [(assoc-player [map player]
            (assoc map
              (:id player)
              (get-in pool [:players (:id player)])))]
    (reduce assoc-player {} players)))

(defn standings
  "Return a map of each player in the pool sorted by rating"
  [pool]
  {:pre [(contains? pool :players)]}
  (into (sorted-map-by (comp > :rating)) (:players pool)))
