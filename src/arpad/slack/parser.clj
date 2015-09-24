(ns arpad.slack.parser
  (:require [arpad.commands.english :refer [str->Command]]
            [clojure.string :refer [lower-case]]))

(def i-me-my
  #{:i :I
    :me :Me :mE :ME
    :my :My :mY :MY})

(defn- replace-me
  [player user-name]
  {:pre [(contains? player :id)]}
  (if (i-me-my (:id player))
    (assoc player :id (keyword user-name))
    player))

(defn- replace-user
  "If the command makes reference to a user named 'I', 'me', or 'my',
  replace those strings with the user-name"
  [command user-name]
  (case (:cmd command)
    :new-game
    (let [[player-a player-b score] (:score command)]
      {:cmd :new-game
       :score [(replace-me player-a user-name)
               (replace-me player-b user-name)
               score]})

    :follow
    {:cmd :follow
     :player (replace-me (:player command) user-name)}

    :ignore
    {:cmd :ignore
     :player (replace-me (:player command) user-name)}

    :rating
    {:cmd :rating
     :player (replace-me (:player command) user-name)}

    command))

(defn- not-error?
  [command]
  (if-not (:error command) command))

(defn parse-slack-request
  "Given a form parsed from the query string of a POST from the Slack
  Outgoing webhooks api, return an arpad-form command.

  If the command is poorly-formatted, return nil"
  [form]
  (let [msg (get form :text)
        user (get form :user_name)
        cmd (str->Command msg)]
    (-> msg
        (lower-case)
        (str->Command)
        (replace-user user)
        (not-error?))))
