(ns sssb-watch.core-test
  (:require [clojure.test :refer :all]
            [sssb-watch.core :refer :all]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(deftest http
  (testing "HTTP package, connection and remote"
    (is (string? (download-data)))))

(def stub-response (slurp "/Users/andreas/Documents/sssb-watch/response"))

(deftest getjson
  (testing "json extracted from response"
    (is (vector? (parse-n-extract stub-response)))))
