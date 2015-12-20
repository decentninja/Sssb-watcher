(ns sssb-watch.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client])
  (:gen-class))

(defn download-data []
  (get (client/get "http://sootn.se") :body))

(defn parse-n-extract [raw]
  (get-in 
    (json/read-str
      (clojure.string/replace
        (clojure.string/replace raw ");" "")
        "jQuery172047263744708705435_1450647604905(" ""))
    ["data" "objektlistabilder@lagenheter" "objekt"]))
  
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
