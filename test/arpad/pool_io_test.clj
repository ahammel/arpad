(ns arpad.pool-io-test
  (:require [clojure.test    :refer :all]
            [clojure.java.io :as io]
            [arpad.pool.load :refer :all]
            [arpad.persistor :refer :all]))

(def filename "foo")
(def backup (str filename ".bak"))

(defn clean-file
  [test-fn]
  (test-fn)
  (io/delete-file filename true)
  (io/delete-file backup true))

(use-fixtures :each clean-file)

(deftest pool-io
  (testing "empty pool round trip"
    (let [pool {:players {}
                :k :fide
                :default-rating 0}]
      (persist filename pool)
      (is (= pool (load-pool filename)))))
  (testing "one player pool round trip"
    (let [pool {:players {:bob {:rating 1000}}
                :k :fide
                :default-rating 1000}]
      (persist filename pool)
      (is (= pool (load-pool filename)))))
  (testing "two player rich data pool round trip"
    (let [pool {:players {:bob {:rating 1000 :ignore? true}
                          :sally {:rating 2000
                                  :total-games 500
                                  :peak-rating 2500}}
                :k :fide
                :default-rating 100}]
      (persist filename pool)
      (is (= pool (load-pool filename))))))
