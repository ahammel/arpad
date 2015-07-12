(ns arpad.commands.schema
  (:require [schema.core :as s]))

(def Score
  (s/both s/Num
          (s/pred (partial >= 0) ">= 0")
          (s/pred (partial <= 1) "<= 1")))

(def Command
  (s/conditional
   #(contains? % :new-game)
   {(s/required-key :new-game) [(s/one s/Keyword "player-a")
                                (s/one s/Keyword "player-b")
                                (s/one Score     "score")]}

   #(contains? % :follow)
   {(s/required-key :follow) s/Keyword}

   #(contains? % :ignore)
   {(s/required-key :ignore) s/Keyword}

   #(contains? % :standings)
   {(s/required-key :standings) (s/maybe s/Num)}))
