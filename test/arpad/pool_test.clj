(ns arpad.pool-test
  (:require [clojure.test      :refer :all]
            [arpad.test-common :refer [close?]]
            [arpad.pool        :refer :all]))

(def empty-pool
  {:players        {}
   :k              (constantly 40)
   :default-rating 1400})

(def alice-bob-pool
  {:players        {:cal   {:rating 1400}
                    :alice {:rating 1500}}
   :k              (constantly 40)
   :default-rating 1000})

(deftest update-pool-test
  (testing "empty pool win"
    (let [pool (update-pool empty-pool {:id :bob} {:id :joe})]
      (is (close? 1420 (get-in pool [:players :bob :rating])))
      (is (close? 1380 (get-in pool [:players :joe :rating])))))
  (testing "empty pool draw"
    (let [pool (update-pool empty-pool {:id :bob} {:id :joe} 0.5)]
      (is (close? 1400 (get-in pool [:players :bob :rating])))
      (is (close? 1400 (get-in pool [:players :joe :rating])))))
  (testing "Alice beats Cal"
    (let [pool (update-pool alice-bob-pool {:id :alice} {:id :cal})]
      (is (close? 1514 (get-in pool [:players :alice :rating]) 1))
      (is (close? 1385 (get-in pool [:players :cal   :rating]) 1)))))

(deftest lookup-players-test
  (testing "lookup Alice"
    (is (= (lookup-player alice-bob-pool {:id :alice})
           {:alice {:rating 1500}})))
  (testing "lookup Alice, Bob, and Cal"
    (is (= (lookup-player alice-bob-pool
                          {:id :alice}
                          {:id :bob}
                          {:id :cal})
           {:alice {:rating 1500}
            :bob   nil
            :cal   {:rating 1400}})))
  (testing "Report standings"
    (is (= (standings alice-bob-pool)
           (sorted-map :alice {:rating 1500}
                       :cal   {:rating 1400})))))
