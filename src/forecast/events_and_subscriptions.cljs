(ns forecast.events-and-subscriptions
    (:require [re-frame.core :as re-frame]
              [reagent.ratom :refer [reaction]]
              [cljs.core.async :as async
               :refer [>! <! put! chan alts! close!]]
               [forecast.views :as views]
               [forecast.utility :refer [push-forward]] ))



;This is used to restrict the forecast display to up
;to 5 places. Can be easily extended/costumized
(defn remove-element
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(def dummy-gadget  (take 5 (repeat {:date 1488747600 
                     :weather-id "scatered clouds"
                     :icon-code "802"
                     :temperature "283.2"
                     :city "221B Baker Street"})))
;An empty app state. Consist of a vector to hold the
;weather info in maps, and a message to display in case;of failure

(def nil-state
  {:gadget-list (into [] (take 5 (repeat nil))) 
   :query-message "Enter a location for a 5 day forecast"})

;self explanatory
 (def initial-state 
   (assoc nil-state :gadget-list
          (push-forward dummy-gadget   
                        (:gadget-list nil-state))))



;subscription section
;when an item changes render!
(re-frame/reg-sub-raw
  :query-list
  (fn 
    [db _]
    (reaction (:query-list @db))))

(re-frame/reg-sub-raw
 :gadget-list
 (fn
   [db _]                          
   (reaction (:gadget-list @db))))   

(re-frame/reg-sub-raw 
  :query-message 
 (fn  [db _]
  (reaction  (:query-message @db))))

;Registered events section

;initialize
(re-frame/reg-event-db
 :initialize-db
 (fn  [db _] (merge  db  initial-state)))

;add a forecast box (weather-map in db, virtualy)
(re-frame/reg-event-db
  :add-forecast
   (fn  [db [_ response] ]
       (update db :gadget-list 
              #(push-forward     response %))))

;update/display error message
(re-frame/reg-event-db
  :change-message
  (fn [db [_ response]]
    (assoc db :query-message response)))

;on click, close a forecast
 (re-frame/reg-event-db
  :remove-forecast
   (fn  [db [_ idx] ]
       (update db :gadget-list 
              #(remove-element % idx))))

;Main render function
(defn render-gadgets
  "The main rendering function"
  []
  (let [gads  (re-frame/subscribe [:gadget-list])]
    (fn []
      (let [gadgets   (filter (complement nil?) @gads)] 
        [:div {:class "cf ph6-l pv6-ns ph1 pv2 ph4-m  center"}   
         [views/search-bar] 
          (if   (empty?  gadgets)
               [:div " "]
               (map-indexed  
                  (fn [idx dc]
                    ^{:key   (str "gadget-" idx)}
                       [views/forecast idx dc])  gadgets))]))))


(defn initialize-app []
  (re-frame/dispatch [:initialize-db]))
