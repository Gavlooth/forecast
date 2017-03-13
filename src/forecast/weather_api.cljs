(ns forecast.weather-api 
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.net.XhrIo :as xhrio]
            [re-frame.core :refer [dispatch]]
            [clojure.string :as st]
            [forecast.utility :as util]
           [clojure.walk :refer [ keywordize-keys]] 
            [cljs.core.async :refer [>! <! put! chan alts! close!]]))
            



(defn geo-info [adr]
  "This is a wrapper for the geocoding facilities of google maps API. Given an 
  address it returns a core.async  channel with all the information of google maps' Geocoder."
  (let [geocoder (js* "new google.maps.Geocoder();" ) 
        address (clj->js {:address  (util/to-query-string adr)}) 
        ch (chan)]
    ((.-geocode geocoder) address 
     (fn geo-results [results status]
       (go   
        (>! ch (if (= "OK" status)
            (js->clj results)
            (str "request failed due to: " status)))
            (close! ch)
        )))
     ch))

 

(defn forecast-info [geo-response]
  "Take a geolocation channel, build a url and send an (async) json request to 
  get the weather"
  (go (let [cords (select-keys 
                    (util/geobject->map 
                      (get-in geo-response   [0 "geometry" "location"]))
                    [:lat :lng] )
            uri (util/forecast-request-uri cords)
            fc-ch (chan)]
        (xhrio/send uri (fn [e]
                          (let [result 
                                (js->clj
                                  (js-invoke 
                                    ( .-target e) 
                                    "getResponseJson"))]  
                            (go (>! fc-ch 
                                    (keywordize-keys  result))
                                (close! fc-ch)))))
        fc-ch)))


(defn update-forecast [query]
  "Combines all the above functions to construct a weather gadget 
  or to write a query failed messages. Dispaches the nessesary events"
  (go  (let [geo-response   (<! (geo-info query))  ]
        (if ((complement string?) geo-response) 
          (go (let [weather-response 
                    (<! (<! (forecast-info geo-response)))]   
                 (if   (= "200"  (:cod  weather-response))
                   (dispatch
                     [:add-forecast 
                      (map first 
                           (partition 7  
                                      (util/fetch-data 
                                       weather-response)))])
                   (util/message-with-reset (str weather-response))   
                      
          (util/message-with-reset  (st/replace (st/lower-case  geo-response) "_" " ")   )    
                )))
          (util/message-with-reset
            (st/replace (st/lower-case  geo-response) "_" " ")   )))))
                














 

