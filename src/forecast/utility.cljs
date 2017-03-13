(ns forecast.utility
  (:import [goog.date UtcDateTime DateTime]) 
  (:require [re-frame.core :refer [dispatch]]
             
           [clojure.string :as st]
			[goog.object :refer [getValues  getKeys]]))

(def days-of-week ["Sun" "Mon" "Tue" "Wed" "Thu" "Fr" "Sat"])

(defn convert-time
"Returns a DateTime instance in the UTC time zone corresponding to the given
  number of milliseconds after the Unix epoch."
  [epoch-time]
  (let [utc   (some-> epoch-time UtcDateTime.fromTimestamp )] 
   (str  (get days-of-week 
                 (.getDay utc)) " " 
           (.getDate utc) "-"
           (inc   (.getMonth utc)) "-"
         (subs    (str  (.getFullYear  utc))   2) )))
		 
(defn  message-with-reset [msg]
  "Puts small delay before reverntint to the original 
  message after error occurcion "
  (dispatch [:change-message msg])
  (js/setTimeout (fn [] (dispatch [:change-message "Enter a location for a 5 day forecasty"])) 1500))

(defn push-forward [x v] (into [x] (take 4 v)))

(defn  forecast-request-uri [{:keys [lat lng]}]
 "take a map with the geolocation coordinates and build the uri to acces
 local weather api"
 (str "http://api.openweathermap.org/data/2.5/forecast?lat=" lat "&lon=" lng "&appid=0b2c9c8ac23b3a7595992042a07cd1be")) 
 
 
(defn to-query-string [input-text]
  "replaces all commas and spaces between words with +"
  (st/replace
    (st/trim  input-text) #"[ ,-]+" "+"))

(defn geobject->map [obj]
"A small function that takes the geocoding (location) object
and transforms it to a clojure map :D :D :D" 
  (into {}   (map (fn [[k v]]
                    (when 
                      (= "function" (goog/typeOf v))
                      [(keyword k) (js-invoke obj k)]  )) 
                  (map  (fn [x] [x (aget obj x)] )
                       (getKeys obj)))))

(defn fetch-data  [{{city :name} :city lista :list}]
  "Parses only the relevant data from the response map of
  a succefull weather api query"
   (map  (fn [{ dt :dt {temp :temp}
             :main
             [{:keys [id description]}] :weather }]
      (zipmap [:date :icon-code :temperature :weather-id :city] 
              (vector dt id temp description city)))  lista ))

