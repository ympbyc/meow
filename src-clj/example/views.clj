(ns example.views
  (:require
   [hiccup
    [page :refer [html5]]
    [page :refer [include-js include-css]]]))

(defn index-page []
  (html5
   [:head
    [:title "Hello World"]
    (include-css "/css/kraken.css")
    (include-css "/css/style.css")
    (include-js "/js/main.js")]
   [:body
    [:h1 "Hello World"]
    "<!--" (slurp "src-cljs/meow/meow.cljs") "-->"]))
