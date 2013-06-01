(ns meow.meow
  (:use [webfui.framework :only [launch-app]]
        [webfui.utilities :only [get-attribute]]
        [meow.reduct :only [evaluate dictionary]])
  (:use-macros [webfui.framework.macros :only [add-dom-watch add-mouse-watch]]))


(defn c-log [x]
  (.log js/console x)
  x)



(defn render-word
  "Render pill in the word pool"
  [{:keys [name data]}]
  [:div.word {:mouse          :mouse-src
              :draggable      "true"
              :data-name name
              :data-data data} name])



(defn render-all
  "feed this to webfui.framework/launch-app"
  [state]
  (let [{:keys [words stack dragged-word val]} state
        [x y] (:point dragged-word)]
    [:div#content
     [:header
      [:h1 "Meow"]
      [:i "Con Cat Cat"]]
     [:section
      [:h2 "Predefined Words"]
      [:div#words.demo
       (map render-word words)]]
     [:section
      [:h2 "Stack"]
      [:div#stack.demo {:style {:min-height "2em"}}
       (map (fn [{:keys [name]}] [:div.word {}  name]) stack)]
      [:small {} (clj->js val)]
      [:div
       [:input#quot-name {:watch :quot-name}]
       [:button#clear.btn.btn-blue "Save Quot and Clear the Stack"]]]

     (when dragged-word
       [:div.word {:style {:position "absolute"
                           :top  (- y 10)
                           :left (+ x 5)}}
        (dragged-word :name)])]))



(add-mouse-watch :mouse-src [{:keys [stack] :as state} first-el last-el points]
                 (let [last-el-id                    (-> last-el  second :id)
                       {:keys [data-name data-data]} (second first-el)
                       word                          {:name data-name :data data-data}
                       new-stack  (conj stack word)]
                   (if (and (not (get-attribute first-el :active))
                            (= last-el-id :stack))  ;drop to stack
                     {:dragged-word nil
                      :val   (evaluate (map :data new-stack))
                      :stack new-stack}
                     {:dragged-word {:point (c-log (last points))
                                     :name  (word :name)}})))

(def words
  (concat
   (map (fn [k] {:data k :name (-> k str goog.string.htmlEscape rest)})
        (keys dictionary))
   (map (fn [x] {:data x :name (str x)}) (range 0 20))
   [{:data [] :name "[]"}]))


(launch-app (atom {:words words
                   :stack []
                   :dragged-word nil
                   :edited-quot nil}) render-all)
