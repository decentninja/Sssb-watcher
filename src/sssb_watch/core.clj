(ns sssb-watch.core
  (:require [clojure.data.json :as json]
            [postal.core :as postal]
            [clj-http.client :as client])
  (:gen-class))


(def AREAS-OF-INTEREST ["Roslagstull" "Nyponet" "Forum" "Domus" "Mjölner"])
(def SEARCH-URL "https://www.sssb.se/widgets/?omraden=&objektTyper=BOASL&hyraMax=&actionId=&paginationantal=all&callback=jQuery172047263744708705435_1450647604905&widgets[]=objektsummering%40lagenheter&widgets[]=alert&widgets[]=objektsummering%40lagenheter&widgets[]=objektfilter%40lagenheter&widgets[]=objektsortering%40lagenheter&widgets[]=objektlistabilder%40lagenheter&widgets[]=paginering%40lagenheter&widgets[]=pagineringantal%40lagenheter&widgets[]=pagineringgofirst%40lagenheter&widgets[]=pagineringgonew%40lagenheter&widgets[]=pagineringlista%40lagenheter&widgets[]=pagineringgoold%40lagenheter&widgets[]=pagineringgolast%40lagenheter&_=1450647605589")

(defn download-data [from]
  (get (client/get from)
       :body))

(defn parse-n-extract [raw]
  (get-in 
    (json/read-str (clojure.string/replace (clojure.string/replace raw
                                                                   ");"
                                                                   "")
                                           "jQuery172047263744708705435_1450647604905(" ""))
    ["data" "objektlistabilder@lagenheter" "objekt"]))
  
(defn filter-interesting [data interesting]
  (filter #(.contains interesting
                      (get % "omrade"))
          data))

(defn show-objects [objects]
  (clojure.string/join "\n"
                       (map #(clojure.string/join " "
                                                  [(get % "omrade")
                                                   (get % "adress")
                                                   (get % "typ")])
                            objects)))

(defn set-interval [callback ms]
  (future (while true
                 (do (Thread/sleep ms)
                     (callback)))))

(def already-sent-to-atom (atom (set [])))

(defn filter-already-sent [objects]
  (filter #(not (contains? (deref already-sent-to-atom) (get % "objektNr")))
          objects))

(defn add-to-sent [to-be-sent]
  (swap! already-sent-to-atom
         clojure.set/union
         (set (map #(get % "objektNr")
                   to-be-sent))))

(defn fix-bounces [parsed]
  (reset! already-sent-to-atom
         (clojure.set/intersection (set (map #(get % "objektNr") parsed))
                                   @already-sent-to-atom)))

(defn send-email [to-be-sent]
  (postal/send-message {:host (System/getenv "MAIL_SMTP")
                        :user (System/getenv "MAIL_USER")
                        :pass (System/getenv "MAIL_PASS")
                        :ssl :y}
                       {:from (System/getenv "MAIL_USER")
                        :to (System/getenv "MAIL_RECIVER")
                        :subject "SSSB Watch"
                        :body (clojure.string/join "\n" (map #(get % "detaljUrl") to-be-sent))}))

(defn action! []
  (let [parsed (parse-n-extract (download-data SEARCH-URL))
        to-be-sent (filter-already-sent (filter-interesting parsed
                                                            AREAS-OF-INTEREST))]
    (do (fix-bounces parsed)
        (add-to-sent to-be-sent) 
        (when (not (empty? to-be-sent)) (send-email to-be-sent)))))

(defn check-env [enviroment required]
  (doseq [r required]
    (when (not (contains? enviroment r))
      (throw (Exception. (clojure.string/join ["Enviroment variable " required " is required to run"]))))))

(defn -main
  [& args]
  (do (check-env (System/getenv) ["MAIL_USER" "MAIL_PASS" "MAIL_RECIVER" "MAIL_SMTP"])
      ;(test-email) ;TODO
      (action!)
      (set-interval action! (* (rand-int 60) 60 1000))))
