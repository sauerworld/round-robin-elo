(ns round-robin-elo.simulate
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [round-robin-elo.player :as player]
            [round-robin-elo.matchmaking :as mm]
            [round-robin-elo.elo :as elo]))

(defn previous-opponents
  [player games]
  (let [p-name (player/player-name player)]
    (->> games
         (keep (fn [game]
                 (when-let [player-in-game (->> game
                                                (filter (comp (partial = p-name) player/player-name))
                                                first)]
                   (first (disj game player-in-game)))))
         (group-by player/player-name)
         (vals)
         (map first))))

(defn game-player-names
  "Returns set of the names of the players in a game."
  [game]
  (->> game
       (map player/player-name)
       (set)))

(defn repeated-games?
  "True if there are any repeated games (games between same opponents)
   in games and repeated games."
  [games previous-games]
  (seq (set/intersection (set (map game-player-names games))
                         (set (map game-player-names previous-games)))))

(defn randomly-split
  "Returns xs, shuffled and split into 2 equal collections."
  [xs]
  (->> (shuffle xs)
       (split-at (/ (count xs) 2))))

(defn preferences-for-player
  ([player opponents]
   (make-prefs player opponents nil))
  ([player opponents previous-games]
   (let [low-prefs (previous-opponents player previous-games)]
     (mm/preference-list player opponents low-prefs))))

(defn prefs-for-group
  ([g1 g2]
   (prefs-for-group g1 g2 nil))
  ([g1 g2 previous-games]
   (->> g1
        (map (fn [p]
               [p (preferences-for-player p g2 previous-games)]))
        (into {}))))

(defn choose-winner
  "Chooses winner of a game between p1 and p2 based on the relative
   true-skill."
  [p1 p2]
  (let [win-chance (elo/win-chance (player/player-true-skill p1)
                                   (player/player-true-skill p2))]
    (if (> win-chance (rand))
      p1
      p2)))

(defn resolve-game
  [rankings game]
  (let [[p1 p2] (seq game)]
    (println (player/player-name p1)
             (str "(" (long (player/player-ranking p1)) " - " (rankings p1) ")")
             "vs."
             (player/player-name p2)
             (str "(" (long (player/player-ranking p2)) " - " (rankings p2) ")"))
    (let [winner (choose-winner p1 p2)
          p1-rank (player/player-ranking p1)
          p2-rank (player/player-ranking p2)
          new-p1 (player/player-ranking p1
                                        (if (= p1 winner)
                                          (elo/ranking-after-win p1-rank p2-rank)
                                          (elo/ranking-after-loss p1-rank p2-rank)))
          new-p2 (player/player-ranking p2
                                        (if (= p2 winner)
                                          (elo/ranking-after-win p2-rank p1-rank)
                                          (elo/ranking-after-loss p2-rank p1-rank)))]
      (println "Winner:" (player/player-name winner))
      (println "New rankings: " (player/player-name new-p1) (long (player/player-ranking new-p1))
               (player/player-name new-p2) (long (player/player-ranking new-p2)))
      [new-p1 new-p2])))

(defn make-games
  [players previous-games]
  (let [[boys girls] (randomly-split players)
         boy-prefs (prefs-for-group boys girls previous-games)
         girl-prefs (prefs-for-group girls boys previous-games)]
    (mm/find-matches girls boys girl-prefs boy-prefs)))

;; Doesn't currently handle odd number of players
(defn do-round
  "Does a single round of ranked-round-robin with set of players and
   optional list of previous games. Returns a vector of
   [new-players previous-games]."
  ([players]
   (do-round players nil))
  ([players previous-games]
   (let [games-fn (fn [] (make-games players previous-games))
         games (->> (repeatedly games-fn)
                    (remove (fn [g] (repeated-games? g previous-games)))
                    first)
         rankings (->> players
                       (sort-by player/player-ranking >)
                       (map (fn [r p] [p r]) (rest (range)))
                       (into {}))]
     [(doall (mapcat (partial resolve-game rankings) games)) (concat previous-games games)])))

(defn simulate-tourney
  "Simulates a tournament with given players and number of rounds."
  ([players number-rounds]
   (simulate-tourney players nil number-rounds))
  ([players previous-games number-rounds]
   (if (> 1 number-rounds)
     (do
       (println)
       (println "Tourney ended. Final rankings:")
       (println (str/join "\n" (sort-by player/player-ranking > players))))
     (do
       (println)
       (println "*** Next round ***")
       (println "Remaining rounds:" number-rounds)
       (let [[players' previous-games'] (do-round players previous-games)]
         (simulate-tourney players' previous-games' (dec number-rounds)))))))
