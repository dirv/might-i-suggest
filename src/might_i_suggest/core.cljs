(ns might-i-suggest.core
  (:require [might-i-suggest.find-entry :as find-entry]))

(defn- create-select-option [[text value] select-element]
  (let [option-element (.createElement (.-ownerDocument select-element) "option")]
    (set! (.-value option-element) value)
    (set! (.-text option-element) text)
    (.appendChild select-element option-element)))

(defn- show-results-box [text-box results]
  (let [parent (.-parentNode text-box)
        box (.createElement (.-ownerDocument text-box) "select")]
    (set! (.-id box) "suggestion-box")
    (.setAttribute box "size" 5)
    (doall (map #(create-select-option % box) results))
    (.appendChild parent box)))

(defn- do-search [find-fn evt]
  (if-let [results (find-fn (-> evt (.-target) (.-value)))]
    (show-results-box (.-target evt) results)))

(defn attach [text-box data]
  (let [find-fn (find-entry/build-finder data)]
    (.addEventListener text-box "change" (partial do-search find-fn))))
