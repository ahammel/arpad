(ns arpad.help)

(def help
  "
Hi there! My name is Arpad, and I'm a bot who keeps track of Elo ratings.

In this help text, I'm assuming your Slack user name is 'alice'.

To have me report your ratings in the standings and after games:
> `arpad follow me` or `arpad follow alice`

In most commands, you can replace your own user name with the words 'I', 'me', or 'my', and I'll figure out who you're talking about.

I can stop reporting your ratings at any time:
> `aprad ignore me` or `arpad unfollow alice`

I'll still keep track of your ratings behind the scenes, in case you decide you'd like to be followed again in the future, and in order to keep track of your opponents ratings.

In order to tell me about a game, use this command:
> `arpad alice beat bob` or `arpad I beat bob`

I'll reply with a message showing your new rating (and Bob's, if I'm tracking his ratings). In order to see this message again, try:
> `arpad my rating`

If you want to see everybody's rating, use:
> `arpad standings`

Or, if that's too many for you, try:
> `arpad top 10`

If you make a mistake, I can revert the last action with:
> `arpad undo`

Have fun!")
