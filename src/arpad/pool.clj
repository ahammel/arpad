(ns arpad.pool
  (:require [arpad.elo :refer [new-ratings]]
            [arpad.k :refer [k-functions]]))

(defn non-nil-max [a b] (if a (max a b) b))

(defn adjust-ratings
  [player-a player-b score k]
  (let [[a' b']
        (new-ratings player-a player-b score k)

        adjust
        (fn [player new-rating]
          (-> player
              (assoc :rating new-rating)
              (assoc :peak-rating (non-nil-max
                                   (:peak-rating player)
                                   new-rating))))]
    [(adjust player-a a') (adjust player-b b')]))

(defn new-player
  [defaults]
  (merge {:rating 0
          :total-games 0
          :peak-rating (if-let [r (:rating defaults)] r 0)
          :ignore? true} defaults))

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

(defn get-k
  [pool]
  {:pre [(contains? pool :k)
         (if (keyword? (:k pool))
           (contains? k-functions (:k pool))
           (number? (:k pool)))]}
  (let [k (:k pool)]
    (if (keyword? k)
      (k k-functions)
      (constantly k))))

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
         k        (get-k pool)
         [a' b']  (adjust-ratings player-a player-b score k)]
     (-> pool'
         (assoc-in [:players (:id winner)] a')
         (assoc-in [:players (:id loser)]  b')))))

(defn lookup-player
  "Return a player's information given an ID. No information is
  returned if the player is being ignored."
  [pool & players]
  {:pre [(contains? pool :players)
         (every? #(contains? % :id) players)]}
  (letfn [(assoc-player [map player]
            (if-not (get-in pool [:players (:id player) :ignore?])
              (assoc map
                (:id player)
                (get-in pool [:players (:id player)]))
              map))]
    (reduce assoc-player {} players)))

(defn- modify-ignore-player
  [pool player ignore-status]
  {:pre [(contains? pool :players)
         (contains? player :id)]}
  (-> pool
      (init-player player)
      (assoc-in [:players (:id player) :ignore?] ignore-status)))

(defn ignore-player
  "Return the pool modified such that the player will be ignored. The
  player will be initialized if not already in the pool"
  [pool player]
  (modify-ignore-player pool player true))

(defn follow-player
  "Modify the pool such that the player's ratings will be
  followed. The player will be initialized if not already in the pool"
  [pool player]
  (modify-ignore-player pool player false))

(defn standings
  "Sort the players in the pool by rating in decreasing order. If a
  limit is given, only that many responses will be generated (not
  counting ignored players)."
  ([pool]
   {:pre [(contains? pool :players)]}
   (->> (:players pool)
        (remove (comp :ignore? second))
        (sort-by (comp :rating val) >)))
  ([pool limit]
   (take limit (standings pool))))
