(ns meow.reduct
  (:require [meow.vocab :as vocab]))


(defn atom? [x]
  (or (number? x)
      (string? x)
      (symbol? x)))

(declare reduct kall dictionary)

(defn visualize [stack]
  (apply str (map (fn [_] "|") stack)))

(defn reduct
  [[x & xs :as quot] stack words]
  ;;(println (visualize stack))
  ;;(println "")
  ;;(println x)
  (if (empty? quot)
    {:stack stack
     :words words}

    (cond
     (atom? x) ;=>
     #(reduct xs (cons x stack) words)

     (= x 'define) ;=>
     #(reduct xs
              (drop 3 stack)
              (conj words {(-> stack rest rest first) (first stack)}))


     (keyword? x) ;=>
     (let [word (words x)
           _  (if (nil? word) (throw (str "unresoleved symbol " x)))
           stk (trampoline kall word stack words)]
      (if (empty? xs)
        {:stack stk :words words} ;tail call?
        #(reduct xs stk words)))

     (coll? x) ;=>
     #(reduct xs (cons x stack) words))))


(defn kall [x stack words]
  (cond
   (fn? x) ;=>
   (x stack words)

   true ;=>
   #(-> (trampoline reduct x stack words) :stack)))

(def dictionary (conj vocab/prelude {:call (fn [[x & xs] ws]
                                             (trampoline kall x xs ws))
                                     :dip (fn [[x y & xs] ws]
                                            (cons y (trampoline kall x xs ws)))
                                     :if (fn [[x y z & xs] ws]
                                           (if (= z :true)
                                             (trampoline kall y xs ws)
                                             (trampoline kall x xs ws)))}))

(defn evaluate [quot]
  ((trampoline reduct quot [] dictionary) :stack))
