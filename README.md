# forecast
Simple forecast web page developed as an APP.


## Overview
#Simple search engine for a 5 day forecast. 


**This app was created using google closure api, reagent reframe, font awesome, tachyons.css**

html css and js resources are located in the resources/public directory and its subdirectories.

Main file is the src/forecast/core.cljs  The forecast namespace also includes:

- forecast.weather-api : Functions to send the nessecary ajax call to get
the forecast data.

- forecast.views : Hiccup components to display the weather and the search form.

- forecast.events-and-subscriptions : Functions to manipulate the events and the state.
of the OPA

- forecast.utility: Varius helper functions (getting the nessecary entries from the
response, format the time e.t.c. e.t.c.).

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2017 Christos Chatzifounts

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
