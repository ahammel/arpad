# arpad

`arpad` is an IRC bot who keeps track of [Elo ratings](https://en.wikipedia.org/wiki/Elo_rating_system). Say "hi", `arpad`.

_waves_

## FAQ

### Why?

For funzies, to learn [clojure.core.async](https://github.com/clojure/core.async), and to determine scientifically who in my office is the best at ping-pong.

### What's with the name?

[Arapd Elo](https://en.wikipedia.org/wiki/Arpad_Elo) was the guy who invented the Elo ratings scheme.

## Installation

`arpad` isn't finished yet. You cannot usefully install him.

## Usage

`arpad` isn't finished yet, and doesn't do anything useful. He tries really hard, though.

## Features

* Keeps track of a pool of players and their Elo ratings
* Automagically adds new players to the pool on their first mention
* Keeps track of which players want their Elo ratings reported on an opt-in basis
* Reports the players' new Elo ratings after a match (if they have opted-in to have their ratings reported)
* Report the standings in the pool on demand (only players who have opted-in are reported)

## Roadmap

### Next

* Persist the pool to disk
* Read the pool from disk at startup
* Parse natual-lanugage-like commands
* [Slack](https://slack.com/) integration

### Some day

* Undo stack
* Multiple pools
* Pairing algorithms (fold, slide, adjacent, random)
* Tournaments (round-robin, Swiss, single/double/treble elimination, group-stage+knockout)

## License

Copyright © 2015 Alex J. Hammel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
