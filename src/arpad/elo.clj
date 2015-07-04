(ns arpad.elo)

(defn- pow [x y] (Math/pow x y))

(defn- expected-score
  [player-a player-b]
  {:pre [(contains? player-a :rating)
         (contains? player-b :rating)]}
  (let [qa (pow 10 (/ (:rating player-a) 400))
        qb (pow 10 (/ (:rating player-b) 400))]
    (/ qa (+ qa qb))))

(defn new-ratings
  [player-a player-b score k]
  {:pre [(contains? player-a :rating)
         (contains? player-b :rating)
         (<= score 1.0)
         (>= score 0.0)]}
  (let [expected (expected-score player-a player-b)
        result-a (- score expected)
        result-b (- (- 1 score) (- 1 expected))
        r'a (+ (:rating player-a) (* (k player-a) result-a))
        r'b (+ (:rating player-b) (* (k player-b) result-b))]
    [r'a, r'b]))


