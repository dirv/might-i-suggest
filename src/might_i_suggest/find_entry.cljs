(ns might-i-suggest.find-entry
  (:require [clojure.string]))

(defn- valid-words [title]
  (filter #(> (.-length %) 2) (clojure.string/split title #" ")))

(defn all-prefixes [word]
  (map (partial subs word 0) (range 3 (inc (.-length word)))))

(defn- build-title-map [[title _ :as entry]]
  (map hash-map (mapcat all-prefixes (valid-words title)) (repeat [entry])))

(defn- build-word-map [data]
  (apply merge-with into (mapcat build-title-map data)))

(defn- find-entry [word-map string]
  (mapcat (partial get word-map) (valid-words string)))

(defn build-finder [data]
  (partial find-entry (build-word-map data)))
