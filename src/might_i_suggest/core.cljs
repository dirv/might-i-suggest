(ns might-i-suggest.core
  (:require [might-i-suggest.find-entry :as find-entry]))

(defn- show-results-box [text-box]
  (let [parent (.-parentNode text-box)
        box (.createElement (.-ownerDocument text-box) "select")]
    (set! (.-id box) "box")
    (.appendChild parent box)))

(defn- do-search [find-fn evt]
  (if-let [results (find-fn (-> evt (.-target) (.-value)))]
    (show-results-box (.-target evt))))

(defn attach [text-box data]
  (let [find-fn (find-entry/build-finder data)]
    (.addEventListener text-box "change" (partial do-search find-fn))))
