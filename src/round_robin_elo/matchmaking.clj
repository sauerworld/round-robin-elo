(ns round-robin-elo.matchmaking
  "Uses the mating algorithm as described here:
   http://ocw.mit.edu/courses/electrical-engineering-and-computer-science/6-042j-mathematics-for-computer-science-fall-2010/video-lectures/lecture-7-matching-problems/MIT6_042JF10_mating.pdf"
  (:require [round-robin-elo.player :as player]))

(defn preference-list
  "For player, return list of opponents in order of preference (closer
   ELO scores are preferred), putting any opponent present in optional
   low-preference  at the bottom of the preference list."
  ([player opponents]
   (preference-list player opponents #{}))
  ([player opponents low-preference]
   (let [opponents-in-low-pref (when (seq low-preference)
                                 (filter (set (map player/player-name opponents))
                                         (map player/player-name low-preference)))]
     (as-> opponents prefs
         (remove (set opponents-in-low-pref) prefs)
         (sort-by (fn [p] (-> (player/player-ranking p)
                             (- (player/player-ranking player))
                             (Math/abs)))
                  prefs)
         (concat prefs opponents-in-low-pref)))))

(defn reject
  "Takes boy-preferences map and a boy, and returns boy-preferences
   map with boy's top choice removed."
  [boy-preferences boy]
  (update boy-preferences boy rest))

(defn choose-favorite-boy
  "Takes a girl, collection of boys, girl-preferences map, and
   boy-preferences map, and chooses her most preferred boy, rejecting
   the others."
  [girl boys girl-preferences boy-preferences]
  (let [girl-choices (->> (girl-preferences girl)
                          (filter (set boys)))]
    (reduce reject boy-preferences (rest girl-choices))))

(defn choose-girls
  [boy-preferences]
  (reduce (fn [choices [boy prefs]]
            (update choices (first prefs) (fnil conj []) boy))
          {}
          boy-preferences))

(defn matchmaking-finished?
  "Takes matches - map of {girl [boy]}. Returns true if
   matchmaking is finished - every girl has at most 1 boy."
  [matches]
  (every? (comp (partial >= 1) count) (vals matches)))

(defn find-matches
  "Takes a collection of \"girl\" players and collection of \"boy\"
   players, optional preference lists for girls and boys, and runs
   mating algorithm to find best matches. Returns collection of
   [player player] matches."
  ([girls boys]
   (find-matches girls boys (preferences girls boys) (preferences boys girls)))
  ([girls boys girl-preferences boy-preferences]
   (let [girls-suitors (choose-girls boy-preferences)]
     (if (matchmaking-finished? girls-suitors)
       (map (comp set (juxt key (comp first val))) girls-suitors)
       (find-matches boys girls girl-preferences
                     (reduce (fn [boy-prefs [girl boys]]
                               (choose-favorite-boy girl boys girl-preferences boy-prefs))
                             boy-preferences
                             girls-suitors))))))
