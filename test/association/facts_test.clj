(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest gapki-has-spec-basis
  (let [sb (facts/spec-basis "gapki")]
    (is (= 2 (count sb)))
    (is (every? #(= "0126" (:association-rule/isic %)) sb))
    (is (every? #(= "IDN" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "vnba")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["gapki" "vnba"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["vnba"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= 2 (count (facts/by-topic "gapki" :governance))))
  (is (empty? (facts/by-topic "gapki" :labor)))
  (is (empty? (facts/by-topic "vnba" :governance))))
