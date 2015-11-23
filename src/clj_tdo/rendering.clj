(ns clj-tdo.rendering
  (:require [clansi.core :refer [style]]
            [clojure.string :as string]))

(defn field
  [label value]
  (str " " label ": " value))

(defn checkbox
  [label checked?]
  (field label (if checked? "[X]" "[ ]")))

(defn style-str
  [string colours]
  (apply style string colours))

(defn pad-right
  [string padding]
  (let [space (fn [width] (string/join "" (take width (repeat " "))))]
    (string/join "" (take padding (str string (space padding))))))
