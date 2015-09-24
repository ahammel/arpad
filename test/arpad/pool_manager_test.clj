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
    (go (>! @in-chan {:cmd :new-game
                      :score [{:id :bob} {:id :mary} 0.5]}))
    (let [report (get-result-out)]
      (is (= (count (:players report)) 0))))
  (testing "follow Bob"
    (go (>! @in-chan {:cmd :follow
                      :player {:id :bob}}))
    (let [result (get-result-out)]
      (is (= result {:following {:id :bob}}))))
  (testing "Bob's results are now reported"
    (go (>! @in-chan {:cmd :standings}))
    (let [result (get-result-out)]
      (is (= (count result) 1))))
  (testing "Followed players are reported in game results"
    (go (>! @in-chan {:cmd :new-game
                      :score [{:id :bob} {:id :mary} 0.5]}))
    (let [result (get-result-out)]
      (is (= (count result) 1))))
  (testing "Ignore Bob again"
    (go (>! @in-chan {:cmd :ignore
                      :player {:id :bob}}))
    (is (= (get-result-out)
           {:ignoring {:id :bob}}))
    (go (>! @in-chan {:cmd :new-game
                      :score [{:id :bob} {:id :mary} 0.5]}))
    (is (= (count (:players (get-result-out)))
           0))))

(deftest pool-manager-tests
  (testing "follow karpov and kasparov"
    (go (>! @in-chan {:cmd :follow
                      :player {:id :kasparov}}))
    (is (= {:following {:id :kasparov}} (get-result-out)))
    (go (>! @in-chan {:cmd :follow
                      :player {:id :karpov}}))
    (is (= {:following {:id :karpov}} (get-result-out))))
  (testing "new players draw"
    (go (>! @in-chan {:cmd :new-game
                      :score [{:id :karpov} {:id :kasparov} 0.5]}))
    (let [result (get-result-out)]
      (is (close? 1000 (get-in result [:players :karpov :rating])))
      (is (close? 1000 (get-in result [:players :kasparov :rating])))
      (is (= 1 (get-in result [:players :karpov :total-games])))
      (is (= 1 (get-in result [:players :kasparov :total-games])))))
  (testing "player report after one game"
    (go (>! @in-chan {:cmd :rating
                      :player {:id :karpov}}))
    (let [result (get-result-out)]
      (is (close? 1000 (get-in result [:players :karpov :rating])))
      (is (close? 1000 (get-in result [:players :karpov :peak-rating])))
      (is (= 1 (get-in result [:players :karpov :total-games])))))
  (testing "kasparov wins"
    (go (>! @in-chan {:cmd :new-game
                      :score [{:id :karpov} {:id :kasparov} 0]}))
    (let [result (get-result-out)]
      (is (> (get-in result [:players :kasparov :rating])
             (get-in result [:players :karpov :rating])))))
  (testing "player report after two game"
    (go (>! @in-chan {:cmd :rating
                      :player {:id :karpov}}))
    (let [result (get-result-out)]
      (is (> 1000 (get-in result [:players :karpov :rating])))
      (is (close? 1000 (get-in result [:players :karpov :peak-rating])))
      (is (= 2 (get-in result [:players :karpov :total-games]))))
    (go (>! @in-chan {:cmd :rating
                      :player {:id :kasparov}}))
    (is (< 1000 (get-in (get-result-out) [:players :kasparov :peak-rating]))))
  (testing "standings"
    (testing "no limit"
      (go (>! @in-chan {:cmd :standings}))
      (let [result (get-result-out)]
        (is (= (into [] (map first (:standings result)))
               [:kasparov :karpov]))))
    (testing "limit of one"
      (go (>! @in-chan {:cmd :standings :number 1}))
      (let [result (get-result-out)]
        (is (= (into [] (map first (:standings result)))
               [:kasparov]))))))

(deftest undo-tests
  (testing "invalid undo"
    (go (>! @in-chan {:cmd :undo}))
    (is (= (get-result-out) {:error :cannot-undo})))
  (testing "undo follow"
    (go (>! @in-chan {:cmd :follow
                      :player {:id :kasparov}}))
    (is (not (nil? (get-result-out))))
    (go (>! @in-chan {:cmd :undo}))
    (is (= (get-result-out) {:undo 1}))
    (go (>! @in-chan {:cmd :standings}))
    (is (= 0 (count (:players (get-result-out))))))
  (testing "undo new game"
    (go (>! @in-chan {:cmd :follow
                      :player {:id :kasparov}}))
    (get-result-out)
    (go (>! @in-chan {:cmd :new-game
                      :score [{:id :karpov} {:id :kasparov} 0]}))
    (get-result-out)
    (go (>! @in-chan {:cmd :undo}))
    (is (= (get-result-out) {:undo 1}))
    (go (>! @in-chan {:cmd :rating
                      :player {:id :kasparov}}))
    (let [result (get-result-out)]
      (is (close? 1000 (get-in result [:players :kasparov :rating])))
      (is (= 0 (get-in result [:players :kasparov :total-games]))))))
