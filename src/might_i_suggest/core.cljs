(ns might-i-suggest.core
  (:require [might-i-suggest.find-entry :as find-entry]))

(def max-suggestions 5)

(defn- create-link [document title on-click-fn]
  (let [link (.createElement document "li")]
    (.appendChild link (.createTextNode document title))
    (.addEventListener link "click" on-click-fn)
    link))

(defn- add-list-item [[title url] container on-select-fn]
  (let [owner (.-ownerDocument container)
        link (create-link owner title #(on-select-fn url))]
    (.appendChild container link)))

(defn- show-suggestions-box [text-box results on-select-fn]
  (let [parent (.-parentNode text-box)
        box (.createElement (.-ownerDocument text-box) "ol")]
    (set! (.-id box) "suggestion-box")
    (doall (take max-suggestions (map #(add-list-item % box on-select-fn) results)))
    (.appendChild parent box)))

(defn- hide-suggestions-box [text-box]
  (when-let [box (.getElementById (.-ownerDocument text-box) "suggestion-box")]
    (.remove box)))

(defn- do-auto-search [find-fn on-select-fn evt]
  (let [results (find-fn (-> evt (.-target) (.-value)))]
    (if (not= [] results)
      (show-suggestions-box (.-target evt) results on-select-fn)
      (hide-suggestions-box (.-target evt)))))

(defn- show-search-results [results-box results on-select-fn]
  (doall
    (for [result results]
      (add-list-item result results-box on-select-fn))))

(defn- do-search [find-fn results-box on-select-fn text-box]
  (let [results (find-fn (.-value text-box))]
    (show-search-results results-box results on-select-fn)))

(defn attach [text-box search-button results-box data on-select-fn]
  (let [find-fn (find-entry/build-finder data)]
    (doall
      (.addEventListener search-button "click" (fn [] (do-search find-fn results-box on-select-fn text-box)))
      (.addEventListener text-box "change" (partial do-auto-search find-fn on-select-fn)))))
