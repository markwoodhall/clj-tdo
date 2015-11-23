(ns clj-tdo.stores
  (:require [clojure.java.io :refer [as-file make-parents]]))

(defprotocol Store
  "An abstraction of a task storage mechanism"
  (put-task
    [this task]
    "Put a new task into the store")
  (get-tasks
    [this]
    "Gets a collection of tasks from the store"))

(defrecord LocalDiskStore
  [path]
  Store
  (put-task
    [this task]
    (let [m (read-string (slurp path))
          tasks (:tasks m)
          tasks (filter (fn [{:keys [id]}]
                          (not= id (:id task))) tasks)
          tasks (conj tasks task)]
      (spit path (pr-str (assoc m :tasks tasks)))))
  (get-tasks
    [this]
    (let [m (read-string (slurp path))
          tasks (:tasks m)]
      tasks)))

(defn local-disk-store
  [path]
  (let [path (str (System/getProperty "user.home") "/clj-tdo/" path)]
    (if (not (.exists (as-file path)))
      (do
        (make-parents path)
        (spit path {:tasks []})))
    (->LocalDiskStore path)))
