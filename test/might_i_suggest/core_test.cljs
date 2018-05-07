(ns might-i-suggest.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [jsdom]
            [spy.core :as spy]
            [might-i-suggest.core :as core]
            [might-i-suggest.find-entry :as find-entry]))

(enable-console-print!)

(defn- create-dom [] (jsdom/JSDOM.))

(defn- build-text-box []
  (let [document (-> (create-dom) (.-window) (.-document))
        form (.createElement document "form")
        text-box (.createElement document "input")]
      (.appendChild form text-box)
      (.appendChild (.-body document) form)
      text-box))

(defn- set-value [input-element value]
  (set! (.-value input-element) value)
  (let [document (.-ownerDocument input-element)
        evt (.createEvent document "HTMLEvents")]
    (.initEvent evt "change" false true)
    (.dispatchEvent input-element evt)))

(deftest attach
  (testing "builds a finder when attaching"
    (let [text-box (build-text-box)
          spy (spy/stub :find-fn)]
      (with-redefs [find-entry/build-finder spy]
        (core/attach text-box [["test" "/a/b"]])
        (is (spy/called? spy))
        (is (spy/called-with? spy [["test" "/a/b"]])))))
  (testing "calls finder function when text is input"
    (let [text-box (build-text-box)
          spy (spy/stub [])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (core/attach text-box [["test" "/a/b"]])
        (set-value text-box "abc")
        (is (spy/called? spy))
        (is (spy/called-with? spy "abc")))))
  (testing "displays suggestion list if there are matches"
    (let [text-box (build-text-box)
          document (.-ownerDocument text-box)
          spy (spy/stub [["title" "/a/b"]])
          finder-spy (spy/stub spy)]
      (with-redefs [find-entry/build-finder finder-spy]
        (core/attach text-box [["test /a/b"]])
        (set-value text-box "abc")
        (is (not (nil? (.getElementById document "box"))))))))
