(ns forecast.views
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :as re-frame]
            [goog.string :as gs]
          [goog.string.format] 
           [reagent.core :as r]
          [forecast.utility :refer [convert-time fetch-data]] 
            [cljs.core.async :as async
             :refer [>! <! put! chan alts! close!]]
            [forecast.weather-api :refer [forecast-info
                                          update-forecast
                                          geo-info]] ))





;Query response message component
;Will desplay a sort message for 1500 if the search query fails 
(defn query-message []
(let [message (re-frame/subscribe [:query-message])] 
  (fn [] 
  [ :table {:class "tbl center mv3-ns mv2 pv2"} 
   [:tbody
    [:tr {:on-change (fn [] (js/console.log "changed"))} @message ]]])))


; The main search bar component. Nothing funcy
(defn search-bar []
 (let [query (r/atom " ") ] 
  (letfn [ (submit-query   [] 
             (update-forecast @query) (reset! query " ") )
          (on-submit  [e]  (.preventDefault e)  (submit-query) )]  
 (fn [] 
 [:form    { :on-submit on-submit }    
  [:table {:class "tbl center mv4-ns mv2 pv1"}
   [:tbody 
    [:tr 
     [:td {:class "td1"}
      [:input {:type "text", 
               :placeholder "search here", 
               :name "search_query",
               :class "sr"
               :value @query
               :on-change #(reset! query (-> % .-target .-value)) }]]
     [:td {:class "td2"}
      [:div {:class "btn1" :on-click #(submit-query)}
       [:i {:class "fa fa-plus fa-2x"}]
       [:div  ]]] ]]]
  [query-message]]))))


;Container to hold the forecast information 

(defn forecast-box [k date weather-code
                    temperature forecast]
 [:table {:id k :class "center  w-100 w-50-m w-20-l collapse tc fl ba br2 b--black-10 pv2 ph2-m"}
  [:tbody  
   [:tr {:class "striped--light-gray ph2 h3 "}
    [:td {:class "pv2 ph2"}  (convert-time (* 1000 date)) ]]
   [:tr {:class "striped--light-gray"}
    [:th {:class "pv2 ph2 f6 fw6 tc ttu"}
     [:i {:class (str  "owf owf-"weather-code" owf-4x")}]] ]
   [:tr {:class "striped--light-gray h2"}
    [:td {:class "pv2 ph2"} (str temperature   " ℃")]]
   [:tr {:class "striped--light-gray  h3"}
    [:td {:class "pv2 ph2  "} forecast]]]]
   )



;The forecast gadget component. Used to displys 
;date, weather icon, sort weather description and temperature

(defn forecast 
  [k weather-map]
  (letfn [(close-gadget [] (re-frame/dispatch [:remove-forecast k]))] 
  [:table {:class  " mv5-ns mv2 center  mh5  pv1"
           :id (str  "forecast-"  k)  }
   [:tbody 
    [:tr  [:td  (:city (first  weather-map)) ]
     [:td ] [:td]  [:td] 
     [:td  {:unselectable "on"
            :class "unselectable close"
            :on-click #(close-gadget) } "[close]"]] 

    [:tr 
     [:td  (map-indexed
      (fn [idx {:keys [date icon-code
                       weather-id temperature]}]
        ^{:key (str   "fb" k "-" idx)}
      [forecast-box (str   "fb" k "-" idx) 
           date
           icon-code 
           (gs/format "%.2f" 
                      (- temperature 273.15))
           weather-id]) 
      weather-map) ]]]]))





















