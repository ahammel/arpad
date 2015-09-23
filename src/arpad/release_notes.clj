(ns arpad.release-notes)

(def latest
  "
Arpad release alpha-0.0.3

  New features:
    * 'arpad undo'
      * Make a mistake? 'Accidentally' spell your opponent's name
        wrong after you lost? Again? Not to worry, I can now reset the
        standings to the last state.
      * Sorry, but I only remeber one previous state. If you make two
        mistakes in a row, you're out of luck.
    * Case-insensitive command parsing
      * Now I can not only understand commands like 'arpad I beat bob',
        but also 'Arpad i beat Bob', ARPAD I BEAT BOB!!!!1!!!!LOL!',
        and 'ArPaD aLiCe bEaT BoB'

  Bug fixes:
    * The peak rating feature should work properly now.

")

(def alpha-0-0-2
  "
Arpad release alpha-0.0.2

  New features:
    * General improvements to player statistics reporting
    * 'arpad: my rating' command
    * I now report total number of games played and peak rating
")

(def alpha-0-0-1
  "
Arpad release alpha-0.0.1

  New features:
    * I now understand first person pronouns!
    * You can use commands like \"arpad: I beat bob\", or \"@arpad
      follow me\", and the \"I\" or \"me\" will be replaced with your
      user name")
