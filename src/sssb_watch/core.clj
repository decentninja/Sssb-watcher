(ns sssb-watch.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client])
  (:gen-class))

(def AREAS-OF-INTEREST ["Roslagstull" "Nyponet" "Forum" "Domus" "Mjölner"])
(def SEARCH-URL "https://www.sssb.se/widgets/?omraden=&objektTyper=BOASL&hyraMax=&actionId=&paginationantal=all&callback=jQuery172047263744708705435_1450647604905&widgets[]=objektsummering%40lagenheter&widgets[]=alert&widgets[]=objektsummering%40lagenheter&widgets[]=objektfilter%40lagenheter&widgets[]=objektsortering%40lagenheter&widgets[]=objektlistabilder%40lagenheter&widgets[]=paginering%40lagenheter&widgets[]=pagineringantal%40lagenheter&widgets[]=pagineringgofirst%40lagenheter&widgets[]=pagineringgonew%40lagenheter&widgets[]=pagineringlista%40lagenheter&widgets[]=pagineringgoold%40lagenheter&widgets[]=pagineringgolast%40lagenheter&_=1450647605589")

(defn download-data []
  (get (client/get SEARCH-URL) :body))

(defn parse-n-extract [raw]
  (get-in 
    (json/read-str
      (clojure.string/replace
        (clojure.string/replace raw ");" "")
        "jQuery172047263744708705435_1450647604905(" ""))
    ["data" "objektlistabilder@lagenheter" "objekt"]))
  
(defn filter-interesting [data interesting]
  (filter #(.contains interesting (get % "omrade"))
          data))

(defn show-objects [objects]
  (clojure.string/join "\n"
                       (map #(clojure.string/join " "
                                                  [(get % "omrade")
                                                   (get % "adress")
                                                   (get % "typ")])
                            objects)))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
