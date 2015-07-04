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
