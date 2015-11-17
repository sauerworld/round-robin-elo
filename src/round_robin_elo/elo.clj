(ns round-robin-elo.elo
  "Implementation of the standard ELO algorithm.")

(def default-max-change
  "The greatest ranking change possible from a game.
   For standard ELO, this is meant to be 16 or 32. "
  50)

;; often called "expected" in standard implementations
(defn win-chance
  "Returns the chance of player winning against opponent."
  [player-ranking opponent-ranking]
  (-> (- opponent-ranking player-ranking)
     (/ 400)
     (->> (Math/pow 10)
          (+ 1)
          (/ 1.0))))

(defn ranking-after-win
  ([player-ranking opponent-ranking]
   (ranking-after-win player-ranking opponent-ranking default-max-change))
  ([player-ranking opponent-ranking max-change]
   (->> (win-chance player-ranking opponent-ranking)
        (- 1)
        (* max-change)
        (+ player-ranking))))

(defn ranking-after-loss
  ([player-ranking opponent-ranking]
   (ranking-after-loss player-ranking opponent-ranking default-max-change))
  ([player-ranking opponent-ranking max-change]
    (->> (win-chance player-ranking opponent-ranking)
         (- 0)
         (* max-change)
         (+ player-ranking))))
