(ns round-robin-elo.player
  "Basic datastructures around player.")

(defn new-player
  "Returns a new player (map datastructure.)

   Name needs to be unique.

   Meaning of name is obvious, ranking is player's current
   in-tournament ranking, true-skill is a number representing the
   player's \"real\" ELO score. In a real implementation we don't have
   this number, it doesn't exist. Here it's used only for simulation
   purposes - players will higher true-skill will win against players
   with lower true-skill more often than not."
  [name true-skill ranking]
  {:name name
   :true-skill true-skill
   :ranking ranking})

(defn player-name
  "Lens-like function: 1-arg returns player name, 2-arg sets it."
  ([p]
   (:name p))
  ([p new-name]
   (assoc p :name new-name)))

(defn player-ranking
  "Lens-like function; 1-arg returns player ranking, 2-arg sets it."
  ([p]
   (:ranking p))
  ([p new-ranking]
   (assoc p :ranking new-ranking)))

(defn player-true-skill
  "Lens-like function; 1-arg returns player true skill, 2-arg sets it."
  ([p]
   (:true-skill p))
  ([p new-skill]
   (assoc p :true-skill new-skill)))
