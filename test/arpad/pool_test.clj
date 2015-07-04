(ns arpad.pool-test
  (:require [clojure.test :refer :all]
            [arpad.test-common :refer [close?]]
            [arpad.pool :refer :all]))

(def empty-pool
  {:players        {}
   :k              (constantly 40)
   :default-rating 1400})

(deftest pool-test
  (testing "empty pool win"
    (let [pool (update-pool empty-pool {:id :bob} {:id :joe})]
      (is (close? 1420 (get-in pool [:players :bob :rating])))
      (is (close? 1380 (get-in pool [:players :joe :rating])))))
  (testing "empty pool draw"
    (let [pool (update-pool empty-pool {:id :bob} {:id :joe} 0.5)]
      (is (close? 1400 (get-in pool [:players :bob :rating])))
      (is (close? 1400 (get-in pool [:players :joe :rating]))))))
