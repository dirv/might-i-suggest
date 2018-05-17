(ns might-i-suggest.core
  (:require [might-i-suggest.find-entry :as find-entry]))

(def max-suggestions 5)

(defn- create-select-option [[title url] select-element on-select-fn]
  (let [option-element (.createElement (.-ownerDocument select-element) "option")]
    (set! (.-value option-element) url)
    (set! (.-text option-element) title)
    (.appendChild select-element option-element)))

(defn- show-results-box [text-box results on-select-fn]
  (let [parent (.-parentNode text-box)
        box (.createElement (.-ownerDocument text-box) "select")]
    (set! (.-id box) "suggestion-box")
    (.setAttribute box "size" max-suggestions)
    (doall (take max-suggestions (map #(create-select-option % box on-select-fn) results)))
    (.addEventListener box "change"
                       (fn [] (on-select-fn (.-value box))))
    (.appendChild parent box)))

(defn- hide-results-box [text-box]
  (let [parent (.-parentNode text-box)]
    (when-let [select-box (.querySelector parent "select")]
      (.removeChild parent select-box))))

(defn- do-search [find-fn on-select-fn evt]
  (let [results (find-fn (-> evt (.-target) (.-value)))]
    (if (not= [] results)
      (show-results-box (.-target evt) results on-select-fn)
      (hide-results-box (.-target evt)))))

(defn- add-search-result [results-box [title url] on-select-fn]
  (let [owner (.-ownerDocument results-box)
        list-item (.createElement owner "li")
        link (.createElement owner "a")]
    (.appendChild link (.createTextNode owner title))
    (.addEventListener link "click" (fn [] (on-select-fn url)))
    (.appendChild list-item link)
    (.appendChild results-box list-item)))

(defn- show-search-results [results-box results on-select-fn]
  (doall
    (for [result results]
      (add-search-result results-box result on-select-fn))))

(defn- do-results [find-fn results-box on-select-fn text-box]
  (let [results (find-fn (.-value text-box))]
    (show-search-results results-box results on-select-fn)))

(defn attach [text-box search-button results-box data on-select-fn]
  (let [find-fn (find-entry/build-finder data)]
    (doall
      (.addEventListener search-button "click" (fn [] (do-results find-fn results-box on-select-fn text-box)))
      (.addEventListener text-box "change" (partial do-search find-fn on-select-fn)))))
