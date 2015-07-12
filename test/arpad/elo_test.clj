(ns arpad.elo-test
  (:require [clojure.test :refer :all]
            [arpad.test-common :refer [close?]]
            [arpad.elo :refer [new-ratings]]))

(deftest test-new-ratings
  (testing "1400 beats 1400 k=40"
    (let [[a b] (new-ratings {:rating 1400}
                             {:rating 1400}
                             1
                             (constantly 40))]
      (is (close? a 1420))
      (is (close? b 1380))))
  (testing "1400 beats 1400 k=20"
    (let [[a b] (new-ratings {:rating 1400}
                             {:rating 1400}
                             1
                             (constantly 20))]
      (is (close? a 1410))
      (is (close? b 1390))))
  (testing "1000 beats 1400 k=30"
    (let [[a b] (new-ratings {:rating 1000}
                             {:rating 1400}
                             1
                             (constantly 30))]
      (is (close? a 1027 1))
      (is (close? b 1372 1))))
  (testing "1400 draws 1400"
    (let [[a b] (new-ratings {:rating 1400}
                             {:rating 1400}
                             0.5
                             (constantly 30) ; doesn't matter
                             )]
      (is (close? a 1400))
      (is (close? b 1400))))
  (testing "1000 draws 1400"
    (let [[a b] (new-ratings {:rating 1000}
                             {:rating 1400}
                             0.5
                             (constantly 40))]
      (is (close? a 1016 1))
      (is (close? b 1383 1)))))
