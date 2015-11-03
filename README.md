# arpad :tomato:

`arpad` is an IRC bot who keeps track of [Elo ratings](https://en.wikipedia.org/wiki/Elo_rating_system). Say "hi", `arpad`.

_waves_

## FAQ

### Why?

For funzies, to learn [clojure.core.async](https://github.com/clojure/core.async), and to determine scientifically who in my office is the best at ping-pong.

### What's with the name?

[Arapd Elo](https://en.wikipedia.org/wiki/Arpad_Elo) was the guy who invented the Elo ratings scheme.

### Why is the logo a tomato?

* 'Arpad' is a Hungrian name
* The only thing I know about Hungary is that paprika is made there
* Paprika is made from red peppers
* There is no red pepper emoji

## Installation

### Ingredients
* One [Slack](https://slack.com/), with Outgoing Webhooks integration
* One tiny server
* One supervisord (optional)

### Method
* `lein uberjar`
* Deploy the standalone jar to the tiny server
* Get it running. I recommend creating a [supervisord task with autorestarts](https://www.digitalocean.com/community/tutorials/how-to-install-and-manage-supervisor-on-ubuntu-and-debian-vps).
* Configure the outgoing webhook to point at `http://tiny.server:1128/v1/arpad`
* Configure the webhook bot however you like. I recommend:
  * User name: `arpad`
  * Trigger word: `arpad, @arpad`
  * Descriptive label: `Elo ratings bot`
  * Icon: :tomato:

## Usage

Assuming your user name is `ajh`

### Track your ratings
`@arpad follow ajh` or `@arpad follow me`

### Report a game
`@arpad ajh beat bobjohnson` or `@arpad ajh beat me` or `@arpad I beat bobjohnson`

### Stop tracking your ratings
`@arpad ignore ajh` or `@arpad ignore me`

### Get the current standings
`@arpad standings`

### Get the top ten
`@arpad top 10`

### Caveats

* Following is opt-in. Your ratings will not be reported until you ask arpad to follow you.
* Even if arpad is ignoring you, your ratings are still tracked behind the scenes. (This is so that the ratings of players who are being followed can be correctly adjusted after matches against players who are not being followed.)
* There is *no support* for slack's @mentions, and you shouldn't use them. I'm working on it.

## Features

* Keeps track of a pool of players and their Elo ratings
* Automagically adds new players to the pool on their first mention
* Keeps track of which players want their Elo ratings reported on an opt-in basis
* Reports the players' new Elo ratings after a match (if they have opted-in to have their ratings reported)
* Report the standings in the pool on demand (only players who have opted-in are reported)

## Roadmap

### Next

* Support for @mentions
* Undo stack
* Pairing algorithms (fold, slide, adjacent, random)

### Some day

* Support for draws
* Multiple pools
* Traditional tournaments (round-robin, Swiss, single/double/treble elimination, group-stage+knockout)
* Continuous tournaments (pyramid, ladder)

## License

Copyright © 2015 Alex J. Hammel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
