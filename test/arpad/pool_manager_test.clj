(ns arpad.pool-manager-test
  (:require [clojure.test       :refer :all]
            [clojure.core.async :refer [>! <! <!!
                                        alts!! close! chan go timeout]]
            [arpad.test-common  :refer [close?]]
            [arpad.pool.manager :refer :all]))

(defn- get-result
  [chan]
  (first (alts!! [chan (timeout 100)])))

(def init-pool {:players {} :k (constantly 40) :default-rating 1000})
(def in-chan (atom nil))
(def out-chans {:player-report (chan)})

(defn with-pool-manager [test-fn]
  (reset! in-chan (chan))
  (spawn-pool-manager init-pool @in-chan out-chans)
  (test-fn)
  (close! @in-chan))

(use-fixtures :each with-pool-manager)

(deftest pool-manager-tests
  (testing "new players draw"
    (go (>! @in-chan {:new-game [{:id :karpov} {:id :kasparov} 0.5]}))
    (let [result (get-result (:player-report out-chans))]
      (is (close? 1000 (get-in result [:karpov   :rating])))
      (is (close? 1000 (get-in result [:kasparov :rating])))
      (is (= 1 (get-in result [:karpov   :total-games])))
      (is (= 1 (get-in result [:kasparov :total-games])))))
  (testing "kasparov wins"
    (go (>! @in-chan {:new-game [{:id :karpov} {:id :kasparov} 0]}))
    (let [result (get-result (:player-report out-chans))]
      (is (> (get-in result [:kasparov :rating])
             (get-in result [:karpov   :rating])))))
  (testing "standings"
    (testing "no limit"
      (go (>! @in-chan {:standings nil}))
      (let [result (get-result (:player-report out-chans))]
        (is (= (into [] (map first result))
               [:kasparov :karpov]))))
    (testing "limit of one"
      (go (>! @in-chan {:standings 1}))
      (let [result (get-result (:player-report out-chans))]
        (is (= (into [] (map first result))
               [:kasparov]))))))
