(ns example.hello
  (:use [webfui.framework :only [launch-app]])
  (:use-macros [webfui.framework.macros :only [add-dom-watch]]))

(defn render-all [state]
  (let [{:keys [a b x-name]} state]
    [:div [:input#a {:watch :watch :value a}]
     " plus "
     [:input#b {:watch :watch :value b}]
     [:p " equals "]
     [:span (+ a b)]

     [:input#x-name {:watch :my-watch :value x-name}]
     [:span x-name]]))

(defn valid-integer [s]
  (and (< (count s) 15) (re-matches #"^[0-9]+$" s)))


(add-dom-watch :watch [state new-element]
               ;;new-element is hiccup data therefore the second element in it will be the attributes
               (let [{:keys [value id]} (second new-element)]
                 (when (valid-integer value)
                   {id (js/parseInt value)})))

(add-dom-watch :my-watch [state new-el]
               (let [{:keys [value id]} (second new-el)]
                 (.log js/console state)
                 (.log js/console value)
                 {id value}))

;;(launch-app (atom {:a 0 :b 0 :x-name 80}) render-all)
