(ns meow.meow
  (:use [webfui.framework :only [launch-app]]
        [webfui.utilities :only [get-attribute]]
        [meow.reduct :only [evaluate dictionary]])
  (:use-macros [webfui.framework.macros :only [add-dom-watch add-mouse-watch]]))



(defn c-log [x]
  (.log js/console (clj->js x))
  x)


(defn el-active? [el]
  (get-attribute el :active))


(defn render-word
  "Render pill in the word pool"
  [watch-id]
  (fn [{:keys [name data id]}]
    [:div.word {:mouse     watch-id
                :draggable "true"
                :data-name name
                :data-data data
                :data-id   id} name]))

(defn render-stack
  "Render a graphical stack based on a given nested stack"
  [stack]
  (if (empty? stack) nil
      (let [[x & xs] stack
            clas     (cond (seq? x)  "seq"
                           (number? x) "num"
                           true      "op")]
        (list [:div {:class (str "stack-val " clas)}
               (if (seq? x)
                 (render-stack x)
                 (js/String (clj->js x)))]
              (render-stack xs)))))



(defn render-all
  "feed this to webfui.framework/launch-app"
  [state]
  (let [{:keys [words note dragged-word val editing-word-name]} state
        [x y] (:point dragged-word)]
    [:div#content
     [:header
      [:h1 "Meow"]
      [:i "Con Cat Cat"]]
     [:div#stack [:div.stack-contents (render-stack val)]]
     [:section
      [:h2 "Predefined Words"]
      [:div#words.demo
       (map (render-word :mouse-vocab) words)]]
     [:section
      [:h2 "Note"]
      [:div#note.demo {:style {:min-height "2em"}}
       (map (render-word :mouse-note) note)]
      [:div
       [:input#quot-name {:watch :quot-name :value editing-word-name}]
       [:button#clear.btn.btn-blue   {:mouse :word-enter} "Save Quot and Clear the Note"]
       [:button#discard.btn.btn-blue {:mouse :word-dismiss} "Discard the Note"]]]

     (when dragged-word
       [:div.word {:style {:position "absolute"
                           :top  (- y 10)
                           :left (+ x 5)}}
        (dragged-word :name)])]))



;watch mouse-move from the vocab to the note
(add-mouse-watch :mouse-vocab [{:keys [note last-id] :as state} first-el last-el points]
                 (let [last-el-id                    (-> last-el  second :id)
                       {:keys [data-name data-data]} (second first-el)
                       word                          {:name data-name
                                                      :data data-data
                                                      :id   (+ last-id 1)}
                       new-note                      (conj note word)]
                   (if (and (not (el-active? first-el))
                            (= last-el-id :note))  ;drop to note
                     {:dragged-word nil
                      :val          (evaluate (map :data new-note))
                      :note         new-note
                      :last-id      (+ last-id 1)}
                     {:dragged-word {:point (last points)
                                     :name  (word :name)}})))


;;mouse-watcher gets invoked at least twice it seems, mousedown & mouseup?

;;remove a word from the note when clicked
(add-mouse-watch :mouse-note [{:keys [note] :as state} first-el last-el points]
                 (let [{:keys [data-id]} (second first-el)
                       new-note          (filter (fn [{:keys [id]}]
                                                   (not= id (js/parseInt data-id))) note)]
                   (if (el-active? first-el)
                     {:val (evaluate (map :data new-note))
                      :note new-note})))


;add word to vocab
(add-dom-watch :quot-name [{:keys [note words] :as state} new-el]
               (let [{:keys [value]} (second new-el)]
                 {:editing-word-name value}))

(add-mouse-watch :word-enter [{:keys [words note editing-word-name]} fe le ps]
                 {:words (conj words {:name editing-word-name :data (map (fn [{:keys [data]}] data) note)})
                  :note  []
                  :val   nil
                  :editing-word-name ""})

;;trash the note
(add-mouse-watch :word-dismiss [& _]
                 {:note [] :val nil :editing-word-name ""})


(def words
  (concat
   (map (fn [k] {:data k :name (-> k str goog.string.htmlEscape rest)})
        (keys dictionary))
   (map (fn [x] {:data x :name (str x)}) (range 0 20))
   [{:data '() :name "[]"}]))


(launch-app (atom {:words words
                   :note         []
                   :dragged-word      nil
                   :edited-quot       nil
                   :val               nil
                   :editing-word-name ""}) render-all)
