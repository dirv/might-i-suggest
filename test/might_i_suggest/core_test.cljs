(ns might-i-suggest.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [jsdom]
            [spy.core :as spy]
            [might-i-suggest.core :as core]
            [might-i-suggest.find-entry :as find-entry]))

(enable-console-print!)

(defn- create-dom [] (jsdom/JSDOM.))

(defn- multi-call-stub [& args]
  (let [remaining-args (atom args)]
    (spy/mock (fn []
                (if (= 1 (count @remaining-args))
                  (first @remaining-args)
                  (let [this-arg (first @remaining-args)]
                    (swap! remaining-args rest)
                    this-arg))))))

(defn- build-document []
  (-> (create-dom) (.-window) (.-document)))

(defn- build-element [element-type document]
  (let [element (.createElement document element-type)]
    (.appendChild (.-body document) element)
    element))

(defn- build-ordered-list [document]
  (let [ordered-list (.createElement document "ol")]
    (.appendChild (.-body document) ordered-list)
    ordered-list))

(defn- raise-event [element event-name]
  (let [document (.-ownerDocument element)
        evt (.createEvent document "HTMLEvents")]
    (.initEvent evt event-name false true)
    (.dispatchEvent element evt)))

(defn- click [input-element]
  (raise-event input-element "click"))

(defn- append-character-to [input-element c]
  (set! (.-value input-element) (str (.-value input-element) c)))

(defn- enter-value [input-element value]
  (set! (.-value input-element) "")
  (doall
    (for [c value]
      (do (append-character-to input-element c)
          (raise-event input-element "keyup")))))

(defn- suggestion-box [text-box]
  (-> text-box
      (.-ownerDocument)
      (.getElementById "suggestion-box")))

(defn- find-text-box [container]
  (.querySelector container "input[type='text']"))

(defn- find-search-button [container]
  (.querySelector container "input[type='button']"))

(def standard-data [["title 1" "/a/b"] ["title 2" "/c/d"]])

(defn- create-and-attach []
  (let [document (build-document)
        container (build-element "div" document)
        results-box (build-ordered-list document)
        click-spy (spy/spy)]
    (core/attach container results-box standard-data click-spy)
    [(find-text-box container) (find-search-button container) results-box click-spy]))

(deftest attach
  (testing "builds a finder when attaching"
    (let [spy (spy/stub :find-fn)]
      (with-redefs [find-entry/build-finder spy]
        (create-and-attach)
        (is (spy/called? spy))
        (is (spy/called-with? spy standard-data)))))
  (testing "calls finder function when text is input"
    (let [spy (spy/stub [])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box & _] (create-and-attach)]
          (enter-value text-box "abc")
          (is (spy/called? spy))
          (is (spy/called-with? spy "abc"))))))
  (testing "displays suggestion list if there are matches"
    (let [spy (spy/stub [["title" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box & _] (create-and-attach)]
          (enter-value text-box "abc")
          (is (not (nil? (suggestion-box text-box))))))))
  (testing "suggestion box lists each page title with url value"
    (let [spy (spy/stub standard-data)
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box & _] (create-and-attach)]
          (enter-value text-box "title")
          (is (= 2 (.-length (.-children (suggestion-box text-box)))))
          (is (= "title 1" (.-textContent (.-firstChild (suggestion-box text-box)))))))))
  (testing "clicking a search result in suggestion box calls select spy"
    (let [spy (spy/stub standard-data)
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box _ _ click-spy] (create-and-attach)]
          (enter-value text-box "title")
          (click (.-firstChild (suggestion-box text-box)))
          (is (spy/called-with? click-spy "/a/b"))))))
  (testing "only show a maximum of 5 suggestions"
    (let [spy (spy/stub (repeat 6 ["title 1" "/a/b"]))
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box & _] (create-and-attach)]
          (enter-value text-box "title")
          (is (= 5 (.-length (.-children (suggestion-box text-box)))))))))
  (testing "calls the on-select-fn when a selection is chosen"
    (let [spy (spy/stub [["title 1" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box _ _ click-spy] (create-and-attach)]
          (enter-value text-box "title")
          (click (.-firstChild (suggestion-box text-box)))
          (is (spy/called-with? click-spy "/a/b"))))))
  (testing "closes the selection box if the search returns nothing"
    (let [spy (multi-call-stub [["title 1" "/a/b"]] [])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box & _] (create-and-attach)]
          (enter-value text-box "title")
          (enter-value text-box "unknown")
          (is (nil? (suggestion-box text-box)))))))
  (testing "never opens the selection box if there's no match"
    (let [spy (spy/stub [])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box & _] (create-and-attach)]
          (enter-value text-box "title")
          (is (nil? (suggestion-box text-box)))))))
  (testing "lists search result when the search button is clicked"
    (let [spy (spy/stub [["title 1" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box search-button results-box _] (create-and-attach)]
          (enter-value text-box "title")
          (click search-button)
          (is (not (nil? (.-firstChild results-box))))
          (is (= "LI" (.-tagName (.-firstChild results-box))))))))
  (testing "search results list items include link with title and url"
    (let [spy (spy/stub [["title 1" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box search-button results-box _] (create-and-attach)]
          (enter-value text-box "title")
          (click search-button)
          (let [link (.-firstChild results-box)]
            (is (= "title 1" (.-textContent link))))))))
  (testing "displays multiple search results"
    (let [spy (spy/stub [["title 1" "/a/b"] ["title 2" "/c/d"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box search-button results-box _] (create-and-attach)]
          (enter-value text-box "title")
          (click search-button)
          (is (= 2 (.-length (.-children results-box))))))))
  (testing "call select fn when clicking search result"
    (let [spy (spy/stub [["title 1" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box search-button results-box click-spy] (create-and-attach)]
          (enter-value text-box "title")
          (click search-button)
          (click (.-firstChild results-box))
          (is (spy/called-with? click-spy "/a/b"))))))
  (testing "sends text box data to search function when searching"
    (let [spy (spy/stub [["title 1" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box search-button results-box click-spy] (create-and-attach)]
          (enter-value text-box "title")
          (click search-button)
          (is (= 6 (count (spy/calls spy))))
          (is (= ["title"] (last (spy/calls spy))))))))
  (testing "replaces suggestion box when searching twice"
    (let [spy (spy/stub [["title 1" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box search-button results-box click-spy] (create-and-attach)]
          (enter-value text-box "title")
          (is (= 1 (count (array-seq (.querySelectorAll (.-ownerDocument text-box) "#suggestion-box")))))))))
  (testing "creates a search button with the text Go"
    (let [spy (spy/stub [])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (let [[text-box search-button results-box click-spy] (create-and-attach)]
          (is (= "Go" (.-value search-button))))))))

