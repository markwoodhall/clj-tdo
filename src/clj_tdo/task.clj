(ns clj-tdo.task
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.string :as string]))

(defrecord Task
  [id title created due done? categories])

(defn overdue?
  [{:keys  [due]}]
  (t/before?  (t/plus (c/from-date due) (t/days 1)) (t/now)))

(defn due-on?
  [{:keys  [due]} date]
  (= date  (c/from-date due)))

(defn finish
  [{:keys  [task due] :as t}]
  (assoc t :done? true))

(defn finished?
  [{:keys [done?]}]
  (= true done?))

(defn create
  [{:keys [id task due categories] repeat-days :repeat}]
  (let [created (c/to-date (t/now))]
    (map (fn [i]
           (let [task (if (= repeat-days 0) task (str i ". " task))]
             (->Task id task created (c/to-date (t/plus due (t/days i))) false (string/split categories #"\|"))))
         (if (= repeat-days 0) [0] (range 0 (+ 1 repeat-days))))))

(comment
  (create {:id "T1"
           :task "test",
           :due (f/parse (f/formatter "dd/MM/yyyy") "12/11/2014"),
           :categories "TEST|TEST2", :repeat 0}))
