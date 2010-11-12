(ns mississippi.web
  (:use compojure.core
        clojure.contrib.json
        ring.adapter.jetty
        ring.middleware.reload
        ring.util.response)
  (:require [mississippi.core :as miss]
            [compojure.route :as route]
            [clojure.walk :as walk]))

(defn- emit-json [x & [status]]
  {:headers {"Content-Type" "application/json"}
   :status (or status 200)
   :body (json-str x)})

(miss/defresource Coffee {:coffee [(miss/member-of #{"latte" "drip"})]
                          :size [(miss/member-of #{"small" "medium" "large"})]
                          :quantity [miss/required (miss/in-range (range 1 4))]})

(defroutes main-routes

  (GET "/orders/:id" [id]
       (emit-json {"coffee" "enjoy"}))

  (POST "/orders"
        {params :params}
        (let [order (Coffee. (walk/keywordize-keys params))]
          (if-let [errors (miss/errors order)]
            (emit-json errors 500)
            (redirect "/orders/1")))))

(def app (-> #'main-routes
             (wrap-reload '[mississippi.web
                            mississippi.core])))

(defn start-server []
  (run-jetty #'app {:port 8080}))


