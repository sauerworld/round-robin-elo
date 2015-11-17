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

(defn make-matcher
  "Takes a player and list of opponents, and returns a matcher in the
   form of [player opponents] where opponents are in order of
   preference."
  ([player opponents]
   [player (preference-list player opponents)])
  ([player opponents previous-opponents]
   [player (preference-list player opponents previous-opponents)]))

(defn player
  "Returns the player part of a suitor or girl."
  [matcher]
  (first matcher))

(defn preferences
  "Returns the preferences part of a suitor or girl."
  [matcher]
  (second matcher))

(defn remove-top-choice
  "Takes a suitor - vector of [player preferences] - and returns the
   matcher with the first preference removed."
  [suitor]
  (update suitor 1 rest))

(defn make-girl-choice
  "Takes a girl and collection of suitors, and returns the suitors
   after girl chooses her most preferred. All but the chosen suitor
   will have girl removed from their preference list."
  [girl suitors]
  (let [suitor-map (zipmap (map player suitors) suitors)
        girl-prefs (->> (preferences girl)
                        (filter (set (keys suitor-map)))
                        (replace suitor-map))]
    (cons (first girl-prefs) (map remove-top-choice (rest girl-prefs)))))

(defn matchmaking-finished?
  "Takes matches - map of {girl [suitor]}. Returns true if
   matchmaking is finished - every girl has at most 1 suitor."
  [matches]
  (every? (comp (partial >= 1) count) (vals matches)))

(defn find-matches
  "Takes a collection of suitors and collection of girls, where each
   suitor and girl is a vector of [player preferences], and runs the
   mating algorithm to find best matches. Returns collection of [player
   player] matches."
  [suitors girls]
  (let [girl-map (zipmap (map player girls) girls)
        girl-suitors (group-by (comp first preferences) suitors)]
    (if (matchmaking-finished? girl-suitors)
      (map (juxt first (comp player first second)) girl-suitors)
      (find-matches (mapcat make-girl-choice
                            (replace girl-map (keys girl-suitors))
                            (vals girl-suitors))
                    girls))))
