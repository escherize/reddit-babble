(ns scrape-reddit.routes.home
  (:require [scrape-reddit.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]
            [hiccup.page :as p]
            [scrape-reddit.markov :refer [sentance]]
            ;; todo: add best of?
            ;;[alandipert.enduro :as e]
            ))

(defn page [& content]
  (p/html5
   (p/include-js
    "https://code.jquery.com/jquery-1.11.3.min.js"
    "https://ajax.aspnetcdn.com/ajax/jquery.validate/1.13.1/jquery.validate.min.js")
   (p/include-css
    "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css"
    "../../../css/flat-ui.css")
   [:div.container content]))

(defn home-page []
  (page
   [:div {:style "text-align:center; margin-top: 150px"}
    [:h1.jumbotron (sentance)]
    [:h4 "This is a randomly generated sentance"]
    [:p "Refresh for another, and you might find a pattern."]]))

(defroutes home-routes
  (GET "/" [] (home-page)))
