(ns clj-tdo.handlers
  (:require [clojure.string :as string]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-tdo.task :as task]
            [clj-tdo.printers :refer [TaskPrinter create-task-printer print-tasks]]
            [clj-tdo.stores :refer [Store put-task get-tasks]]))

(defn list-tasks
  [{:keys [due overdue categories sort finished printer]} store]
  {:pre [(satisfies? Store store)]}
  (let [filter-category? (not-empty categories)
        tasks (get-tasks store)
        tasks (filter #(if (not= nil due) (task/due-on? % due) true) tasks)
        tasks (filter #(if overdue (task/overdue? %) true) tasks)
        tasks (filter #(if filter-category? (.contains (:categories %) categories) true) tasks)
        tasks (filter #(if (not finished) (not (task/finished? %)) true) tasks)
        sort-criteria (case sort
                        "C" :created
                        :due)
        tasks (sort-by sort-criteria #(t/before? (c/from-date %1) (c/from-date %2)) tasks)]
    (print-tasks (create-task-printer printer) tasks println)))

(defn do-task
  [{:keys [id]} store]
  {:pre [(satisfies? Store store)]}
  (let [t (last (filter (fn [{:keys [id]}]
                          (= id id)) (get-tasks store)))]
    (if t
      (put-task store (task/finish t)))
    (list-tasks {} store)))

(defn new-task
  [args store]
  {:pre [(satisfies? Store store)]}
  (doseq [t (task/create args)]
    (put-task store t))
  (list-tasks {} store))

(def handlers
  {:new new-task
   :list list-tasks
   :ls list-tasks
   :done do-task})

(defn all-handlers
  []
  handlers)
