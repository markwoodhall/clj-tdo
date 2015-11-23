(ns clj-tdo.printers
  (:require [clj-tdo.rendering :as r]
            [clj-tdo.task :as t]
            [clj-time.coerce :as c]
            [clojure.string :as string]))

(defprotocol TaskPrinter
  "An abstraction of a printing mechanism"
  (print-task
    [this task output-fn]
    "Print a task to the given output function")
  (print-tasks
    [this tasks ouput-fn]
    "Print a sequence of tasks to the given output function"))

(defrecord DefaultTaskPrinter
  []
  TaskPrinter
  (print-task
    [this {:keys [id title due done? categories] :as t} output-fn]
    (let [overdue? (t/overdue? t)
          id      (r/pad-right id 10)
          title   (r/pad-right title 50)
          due     (r/pad-right (r/field "Due" (c/from-date due)) 16)
          done    (r/checkbox "Done" done?)
          overdue (r/checkbox "Overdue" overdue?)
          tags    (r/field    "Tags" (string/join "|" categories))
          msg     (str id title due done overdue tags)
          styles (filter #(case %
                            :red (and overdue?  (not done?))
                            :green
                            :green (or done? (and overdue? done?))
                            :yellow (and (not overdue?) (not done?))) [:green :red :yellow])]
      (output-fn (r/style-str msg styles)))) 
  (print-tasks
    [this tasks output-fn]
    (doseq [task tasks]
      (print-task this task output-fn))))

(defrecord RawTaskPrinter
  []
  TaskPrinter
  (print-task
    [this task output-fn]
    (output-fn (pr-str task))) 
  (print-tasks
    [this tasks output-fn]
    (doseq [task tasks]
      (print-task this task output-fn))))

(defn create-task-printer
  [printer-type]
  (case printer-type
    "raw" (->RawTaskPrinter)
    (->DefaultTaskPrinter)))

