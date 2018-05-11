(ns might-i-suggest.core
  (:require [might-i-suggest.find-entry :as find-entry]))

(def max-suggestions 5)

(defn- create-select-option [[text value] select-element on-select-fn]
  (let [option-element (.createElement (.-ownerDocument select-element) "option")]
    (set! (.-value option-element) value)
    (set! (.-text option-element) text)
    (.addEventListener option-element "click" (fn [] (on-select-fn value)))
    (.appendChild select-element option-element)))

(defn- show-results-box [text-box results on-select-fn]
  (let [parent (.-parentNode text-box)
        box (.createElement (.-ownerDocument text-box) "select")]
    (set! (.-id box) "suggestion-box")
    (.setAttribute box "size" max-suggestions)
    (doall (take max-suggestions (map #(create-select-option % box on-select-fn) results)))
    (.appendChild parent box)))

(defn- do-search [find-fn on-select-fn evt]
  (if-let [results (find-fn (-> evt (.-target) (.-value)))]
    (show-results-box (.-target evt) results on-select-fn)))

(defn attach [text-box data on-select-fn]
  (let [find-fn (find-entry/build-finder data)]
    (.addEventListener text-box "change" (partial do-search find-fn on-select-fn))))
