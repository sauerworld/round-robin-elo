# round-robin-elo

ELO-based round robin tournament system.

## Outline

This is a tournament format designed to run many simultaneous games
possible, maximize the competitive level of the games (games should be
between people who are close in skill), allow organizers to control
tournament length, and work fairly.

It uses ELO ratings, updated throughout the tournament, as the
underlying mechanism of match-making and tournament rankings.

## Method

The tournament is run as a series of round-robin rounds. Every player
plays in every round, unless there's an odd number of players in which
case a single bye player needs to be selected for each round. (The bye
selection method is outside the scope of this system)

Matches for each round are determined using a matching algorithm based
on ELO score - it randomly divides the players into two equal groups
and attempts to match each group member with the closest-ranked player
in the other group, while disfavoring repeated games with the same
opponent.

After each round, ELO scores are updated based on the results of the
games.

Tournaments can choose the number of rounds they'd like to run. The
trade-off is increasing the number of rounds reduces the impact of
luck while increasing the length of the tournament.

It's also suggested that tournaments use the top 4 ranked competitors
at the end of the rounds and use some form of playoffs to
determine final tournament results.

## Examples

We start with a set of players, and for the purposes of our simulation
we assign them a "true" skill level that will be used to figure out
who wins games.

```clojure
(def player-true-skill
  {"swatllama" 1200
   "Frosty" 1100
   "Acuerta" 1050
   "Honzik" 1070
   "Redon" 1020
   "Raffael" 980
   "lagout" 950
   "neon" 900
   "r3hab" 850
   "Fear" 800})
```

Again, the true skill is present for simulation purposes only. This
number does not exist when the system is used for real tournaments.

We assign a set of starting ELO ranks to all the players, based on
their performance in the all vs. all game. We have defined our ELO
algorithm to have a max delta of 50 points per game, so we have a
50-point spread in the initial rankings.

```clojure
(def player-rankings
  {"swatllama" 1025
   "Frosty" 1020
   "Acuerta" 1015
   "neon" 1010
   "Raffael" 1005
   "Honzik" 995
   "Redon" 990
   "Fear" 985
   "r3hab" 980
   "lagout" 975})
```

We then simulate a tournament lasting for 4 rounds using this player data:

```clojure
(def players
  (map (fn [[name ranking]]
         (player/new-player name ranking (get player-rankings name)))
         player-true-skill))

(round-robin-elo.simulate/simulate-tourney players 4)
```

Here is an example of the output of one simulation:

```repl
*** Next round ***
Remaining rounds: 4
Acuerta (1015) vs. neon (1010)
Winner: Acuerta
New rankings:  Acuerta 1039 neon 985
Honzik (995) vs. Raffael (1005)
Winner: Honzik
New rankings:  Honzik 1020 Raffael 979
r3hab (980) vs. Fear (985)
Winner: Fear
New rankings:  r3hab 955 Fear 1009
Redon (990) vs. lagout (975)
Winner: lagout
New rankings:  Redon 963 lagout 1001
Frosty (1020) vs. swatllama (1025)
Winner: Frosty
New rankings:  Frosty 1045 swatllama 999

*** Next round ***
Remaining rounds: 3
Fear (1009) vs. Honzik (1020)
Winner: Honzik
New rankings:  Fear 985 Honzik 1044
lagout (1001) vs. swatllama (999)
Winner: swatllama
New rankings:  lagout 975 swatllama 1024
neon (985) vs. Acuerta (1039)
Winner: Acuerta
New rankings:  neon 964 Acuerta 1060
Raffael (979) vs. Frosty (1045)
Winner: Frosty
New rankings:  Raffael 958 Frosty 1065
r3hab (955) vs. Redon (963)
Winner: r3hab
New rankings:  r3hab 980 Redon 938

*** Next round ***
Remaining rounds: 2
Acuerta (1060) vs. Honzik (1044)
Winner: Honzik
New rankings:  Acuerta 1034 Honzik 1071
Frosty (1065) vs. Redon (938)
Winner: Redon
New rankings:  Frosty 1031 Redon 972
Raffael (958) vs. neon (964)
Winner: neon
New rankings:  Raffael 934 neon 988
swatllama (1024) vs. lagout (975)
Winner: swatllama
New rankings:  swatllama 1046 lagout 954
Fear (985) vs. r3hab (980)
Winner: Fear
New rankings:  Fear 1010 r3hab 956

*** Next round ***
Remaining rounds: 1
swatllama (1046) vs. Acuerta (1034)
Winner: swatllama
New rankings:  swatllama 1070 Acuerta 1010
Fear (1010) vs. Frosty (1031)
Winner: Fear
New rankings:  Fear 1036 Frosty 1005
Redon (972) vs. neon (988)
Winner: Redon
New rankings:  Redon 998 neon 962
r3hab (956) vs. lagout (954)
Winner: r3hab
New rankings:  r3hab 981 lagout 929
Raffael (934) vs. Honzik (1071)
Winner: Raffael
New rankings:  Raffael 968 Honzik 1036

Tourney ended. Final rankings:
{:name "swatllama", :true-skill 1200, :ranking 1070.4208457310774}
{:name "Honzik", :true-skill 1070, :ranking 1036.7030204748107}
{:name "Fear", :true-skill 800, :ranking 1036.6806598949322}
{:name "Acuerta", :true-skill 1050, :ranking 1010.463267115934}
{:name "Frosty", :true-skill 1100, :ranking 1005.3225177765071}
{:name "Redon", :true-skill 1020, :ranking 998.2860860676807}
{:name "r3hab", :true-skill 850, :ranking 981.164644142724}
{:name "Raffael", :true-skill 980, :ranking 968.7156577593191}
{:name "neon", :true-skill 900, :ranking 962.6497683848725}
{:name "lagout", :true-skill 950, :ranking 929.5935326521419}
```


## License

Copyright © 2015 Michael Gaare

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
