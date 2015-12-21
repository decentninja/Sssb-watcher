(ns sssb-watch.core-test
  (:require [clojure.test :refer :all]
            [sssb-watch.core :refer :all]
            [clj-http.client :as client]
            [postal.core :as postal]
            [clojure.data.json :as json]))


(deftest http
  (testing "HTTP package and connection"
    (is (string? (download-data "http://decent.ninja")))))

(def stub-response
  (slurp "/Users/andreas/Documents/sssb-watch/response"))

(deftest getjson
  (testing "json extracted from response"
    (is (vector? (parse-n-extract stub-response))))0)

(deftest filter-areas
  (testing "filter correct areas"
    (is (= [{"omrade" "Forum"}]
           (filter-interesting [{"omrade" "Forum"}
                                {"omrade" "Blurg"}]
                               ["Forum"])))))

(deftest email
  (testing "sending email"
    (is (= (get (postal/send-message {:host "smtp.gmail.com"
                                      :user "encrypt.wave"
                                      :pass (System/getenv "MAIL_PASS")
                                      :ssl :y}
                                     {:from "encrypt.wave@gmail.com"
                                      :to "lat.zaft@gmail.com"
                                      :subject "Test"
                                      :body "test"}) 
                :message)
           "messages sent"))))

(deftest filter-already
  (testing "is filtering correct"
    (let [x [{"objektNr" 123}]]
      (is (= x (filter-already-sent x)))
      (reset! already-sent-to-atom #{123})
      (is (= [] (filter-already-sent x))))))

(deftest already-sent
  (testing "if already sent works"
    (add-to-sent [{"objektNr" 123}])
    (is (= @already-sent-to-atom #{123}))))

(deftest bounces
  (testing "if an item disapears from data, we should remove it two so that we can again find it..."
    (reset! already-sent-to-atom #{1 2 3})
    (fix-bounces [{"objektNr" 2}])
    (is (= @already-sent-to-atom #{2}))))
