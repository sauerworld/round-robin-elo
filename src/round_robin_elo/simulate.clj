(ns round-robin-elo.simulate
  (:require [clojure.string :as str]
            [round-robin-elo.player :as player]
            [round-robin-elo.matchmaking :as mm]
            [round-robin-elo.elo :as elo]))

(defn previous-opponents
  [player games]
  (let [p-name (player/player-name player)
        opps (keep (fn [[p1 p2]]
                     (cond (= p-name (player/player-name p1))
                           p2
                           (= p-name (player/player-name p2))
                           p1))
                   games)]
    (->> (group-by player/player-name opps)
         (vals)
         (map first))))

(defn make-prefs
  ([players]
   (make-prefs players nil))
  ([players previous-games]
   (let [[boys girls] (split-at (/ (count players) 2)
                                (shuffle players))]
     [(map #(mm/make-matcher % girls) boys)
      (map #(mm/make-matcher % boys) girls)])))

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
  [[p1 p2]]
  (println (player/player-name p1) (str "(" (long (player/player-ranking p1)) ")") "vs."
           (player/player-name p2) (str "(" (long (player/player-ranking p2)) ")" ))
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
    [new-p1 new-p2]))

;; Doesn't currently handle odd number of players
(defn do-round
  "Does a single round of ranked-round-robin with set of players and
   optional list of previous games. Returns a vector of
   [new-players previous-games]."
  ([players]
   (do-round players nil))
  ([players previous-games]
   (let [[boys girls] (make-prefs players previous-games)
         games (mm/find-matches boys girls)]
     [(doall (mapcat resolve-game games)) (concat previous-games games)])))

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
