(ns might-i-suggest.core
  (:require [might-i-suggest.find-entry :as find-entry]))

(def max-suggestions 5)

(defn- build-input-element [input-type container]
  (let [input-element (.createElement (.-ownerDocument container) "input")]
    (.setAttribute input-element "type" input-type)
    (.appendChild container input-element)
    input-element))

(def build-text-box (partial build-input-element "text"))
(def build-button (partial build-input-element "button"))

(defn- create-link [document title on-click-fn]
  (let [link (.createElement document "li")]
    (.appendChild link (.createTextNode document title))
    (.addEventListener link "click" on-click-fn)
    link))

(defn- add-list-item [[title url] container on-select-fn]
  (let [owner (.-ownerDocument container)
        link (create-link owner title #(on-select-fn url))]
    (.appendChild container link)))

(defn- hide-suggestions-box [text-box]
  (when-let [box (.getElementById (.-ownerDocument text-box) "suggestion-box")]
    (.remove box)))

(defn- show-suggestions-box [text-box results on-select-fn]
  (hide-suggestions-box text-box)
  (let [parent (.-parentNode text-box)
        box (.createElement (.-ownerDocument text-box) "ol")]
    (set! (.-id box) "suggestion-box")
    (doall (take max-suggestions (map #(add-list-item % box on-select-fn) results)))
    (.appendChild parent box)))

(defn- do-auto-search [find-fn on-select-fn evt]
  (let [results (find-fn (-> evt (.-target) (.-value)))]
    (if (and results (not= [] results))
      (show-suggestions-box (.-target evt) results on-select-fn)
      (hide-suggestions-box (.-target evt)))))

(defn- show-search-results [results-box results on-select-fn]
  (doall
    (for [result results]
      (add-list-item result results-box on-select-fn))))

(defn- do-search [find-fn results-box on-select-fn text-box]
  (let [results (find-fn (.-value text-box))]
    (show-search-results results-box results on-select-fn)))

(defn attach [container results-box data on-select-fn]
  (let [text-box (build-text-box container)
        search-button (build-button container)
        find-fn (find-entry/build-finder data)]
    (doall
      (.addEventListener search-button "click" (fn [] (do-search find-fn results-box on-select-fn text-box)))
      (.addEventListener text-box "keyup" (partial do-auto-search find-fn on-select-fn)))))
