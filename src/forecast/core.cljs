(ns forecast.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [cljs.core.async :as async
             :refer [>! <! put! chan alts!]]
            [forecast.events-and-subscriptions :as ev]))

  
 
(defn mount-root []
  (ev/initialize-app) 
  (reagent/render [ev/render-gadgets]
                  (.getElementById js/document "app")))

 
(mount-root)



 


