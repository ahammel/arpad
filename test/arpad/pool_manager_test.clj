(ns arpad.pool-manager-test
  (:require [clojure.test       :refer :all]
            [clojure.core.async :refer [>! <! <!!  alts!! close!
                                        dropping-buffer chan go
                                        timeout]]
            [arpad.test-common  :refer [close?]]
            [arpad.pool.manager :refer :all]))

(def init-pool {:players {} :k 40 :default-rating 1000})
(def in-chan (atom nil))
(def out-chans {:player-report (chan)
                :new-state (chan (dropping-buffer 10))})

(defn- get-result
  [chan]
  (first (alts!! [chan (timeout 100)])))

(defn- get-result-out
  []
  (get-result (:player-report out-chans)))

(defn with-pool-manager [test-fn]
  (reset! in-chan (chan))
  (spawn-pool-manager init-pool @in-chan out-chans)
  (test-fn)
  (close! @in-chan))

(use-fixtures :each with-pool-manager)

(deftest ignored-players-tests
  (testing "new players default to ignored"
    (go (>! @in-chan {:new-game [{:id :bob} {:id :mary} 0.5]}))
    (let [report (get-result-out)]
      (is (= (count report) 0))))
  (testing "follow Bob"
    (go (>! @in-chan {:follow {:id :bob}}))
    (let [result (get-result-out)]
      (is (= result {:following {:id :bob}}))))
  (testing "Bob's results are now reported"
    (go (>! @in-chan {:standings nil}))
    (let [result (get-result-out)]
      (is (= (count result) 1))))
  (testing "Followed players are reported in game results"
    (go (>! @in-chan {:new-game [{:id :bob} {:id :mary} 0.5]}))
    (let [result (get-result-out)]
      (is (= (count result) 1))))
  (testing "Ignore Bob again"
    (go (>! @in-chan {:ignore {:id :bob}}))
    (is (= (get-result-out)
           {:ignoring {:id :bob}}))
    (go (>! @in-chan {:new-game [{:id :bob} {:id :mary} 0.5]}))
    (is (= (count (get-result-out))
           0))))

(deftest pool-manager-tests
  (testing "follow karpov and kasparov"
    (go (>! @in-chan {:follow {:id :kasparov}}))
    (is (= {:following {:id :kasparov}} (get-result-out)))
    (go (>! @in-chan {:follow {:id :karpov}}))
    (is (= {:following {:id :karpov}} (get-result-out))))
  (testing "new players draw"
    (go (>! @in-chan {:new-game [{:id :karpov} {:id :kasparov} 0.5]}))
    (let [result (get-result-out)]
      (is (close? 1000 (get-in result [:karpov   :rating])))
      (is (close? 1000 (get-in result [:kasparov :rating])))
      (is (= 1 (get-in result [:karpov   :total-games])))
      (is (= 1 (get-in result [:kasparov :total-games])))))
  (testing "kasparov wins"
    (go (>! @in-chan {:new-game [{:id :karpov} {:id :kasparov} 0]}))
    (let [result (get-result-out)]
      (is (> (get-in result [:kasparov :rating])
             (get-in result [:karpov   :rating])))))
  (testing "standings"
    (testing "no limit"
      (go (>! @in-chan {:standings nil}))
      (let [result (get-result-out)]
        (is (= (into [] (map first result))
               [:kasparov :karpov]))))
    (testing "limit of one"
      (go (>! @in-chan {:standings 1}))
      (let [result (get-result-out)]
        (is (= (into [] (map first result))
               [:kasparov]))))))
