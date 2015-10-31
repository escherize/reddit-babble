(ns scrape-reddit.markov
  (:require [clojure.string :as str]
            [alandipert.enduro :as e]
            [net.cgrand.enlive-html :refer :all]))

(defn word-chain [word-transitions]
  (reduce (fn [r t]
            (merge-with clojure.set/union r
                        (let [[a b c] t]
                          {[a b] (if c #{c} #{})})))
          {}
          word-transitions))

(defn text->word-chain [s]
  (let [words (clojure.string/split s #"[\s|\n]")
        word-transitions (partition-all 3 1 words)]
    (word-chain word-transitions)))

(defn chain->text [chain]
  (apply str (interpose " " chain)))

(defn walk-chain [prefix chain result]
  (let [suffixes (get chain prefix)]
    (if (empty? suffixes)
      result
      (let [suffix (first (shuffle suffixes))
            new-prefix [(last prefix) suffix]
            result-with-spaces (chain->text result)
            result-char-count (count result-with-spaces)
            suffix-char-count (+ 1 (count suffix))
            new-result-char-count (+ result-char-count suffix-char-count)]
        (if (>= new-result-char-count 140)
          result
          (recur new-prefix chain (conj result suffix)))))))

(defn generate-text
  [start-phrase word-chain]
  (let [prefix (clojure.string/split start-phrase #" ")
        result-chain (walk-chain prefix word-chain prefix)
        result-text (chain->text result-chain)]
    result-text))

(def c2 (e/file-atom #{} "resources/comments.clj"))

(def corups (str/join " " (mapv text @c2)))

(def word-chain (text->word-chain corups))

(defn end-at-last-punctuation [text]
  (let [trimmed-to-last-punct (apply str (re-seq #"[\s\w]+[^.!?,]*[.!?,]" text))
        trimmed-to-last-word (apply str (re-seq #".*[^a-zA-Z]+" text))
        result-text (if (empty? trimmed-to-last-punct)
                      trimmed-to-last-word
                      trimmed-to-last-punct)
        cleaned-text (clojure.string/replace result-text #"[,| ]$" ".")]
    (-> cleaned-text
        (clojure.string/replace #"\"" "'")
        str/trim
        str/capitalize)))

(defn first-sentance [multiple-sentances]
  (->> multiple-sentances
       (split-with #(not (#{\. \? \!} %)))
       first ;;first half
       (#(concat % [(rand-nth [\! \! \? \. \. \.])]))
       (apply str)))

(defn- sentance* [word-chain]
  (let [prefix (str/join " " (rand-nth (keys word-chain)))
        raw-sentance (generate-text prefix word-chain)]
    (-> raw-sentance
        end-at-last-punctuation
        first-sentance)))

(defn sentance [& _] (sentance* word-chain))
