(ns user
  (:require [clojure.pprint :refer (pprint)]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.namespace.repl :refer :all]
            [round-robin-elo.elo :as elo]
            [round-robin-elo.matchmaking :as mm]
            [round-robin-elo.player :as player]
            [round-robin-elo.simulate :as sim]))

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

;; order of results from allvall
;;
;; swatllama 1
;; frosty 2
;; acuerta 3
;; neon 4
;; raffael 5
;; honzik 6
;; redon 7
;; fear 8
;; r3hab 9
;; lagout 10


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

(def players
  (map (fn [[name ranking]]
         (player/new-player name ranking (get player-rankings name)))
       player-true-skill))


(defn make-prefs
  [players]
  (let [[boys girls] (split-at (/ (count players) 2)
                               (shuffle players))]
    [(map #(mm/make-matcher % girls) boys)
     (map #(mm/make-matcher % boys) girls)]))
