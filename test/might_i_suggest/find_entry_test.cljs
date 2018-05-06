(ns might-i-suggest.find-entry-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [might-i-suggest.find-entry :as find-entry]))

(def find-fn
  (find-entry/build-finder
    [["cat" "/a/b"]
     ["cake" "/a/b"]
     ["this is a test" "/a/b"]
     ["dog a" "/a/b"]
     ["dog b" "/a/b"]]))

(deftest test-find
  (testing "finds no entries if word does not exist"
    (is (= [] (find-fn ["rat"]))))
  (testing "find single entry for single word"
    (is (= [["cat" "/a/b"]] (find-fn "cat"))))
  (testing "matches prefix of word"
    (is (= [["cake" "/a/b"]] (find-fn "cak"))))
  (testing "matches multiple words"
    (is (= [["this is a test" "/a/b"]] (find-fn "test"))))
  (testing "does not match words with less than three characters"
    (is (= [] (find-fn "is"))))
  (testing "matches multiple pages"
    (is (= [["dog a" "/a/b"]
            ["dog b" "/a/b"]] (find-fn "dog"))))
  (testing "matches multiple words in search term"
    (is (= [["dog a" "/a/b"]
            ["dog b" "/a/b"]
            ["cat" "/a/b" ]] (find-fn "dog cat")))))
