(ns clj-tdo.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-tdo.handlers :refer [all-handlers]]
            [clj-tdo.stores :refer [local-disk-store]]
            [clj-time.format :as f]
            [clj-time.core :as t])
  (:gen-class :main true))

(def cli-options
  [["-t" "--task TASK" "The title of the task."
    :validate [#(> (count %) 0) "Must be a non empty string."]]
   ["-i" "--id ID" "The identifier of the task."
    :validate [#(> (count %) 0) "Must be a non empty string."]]
   ["-d" "--due DUE" "The due date of the task, e.g. 25/12/2014."
    :parse-fn (fn [arg]
                (f/parse (f/formatter "dd/MM/yyyy") arg))]
   ["-r" "--repeat DAYS" "Number of days to repeat the task."
    :default 0
    :parse-fn #(Integer/parseInt %)]
   ["-s" "--sort CRITERIA" "Sort by [D]ue date|[C]reated date."]
   ["-c" "--categories CATEGORIES" "A list of pipe separated categories."]
   ["-p" "--printer PRINTER" "Specify the printer type. raw or default."]
   ["-o" "--overdue" "Only show overdue tasks."]
   ["-f" "--finished" "Show finished tasks."]
   ["-h" "--help"]])

(defn help [options]
  (->> ["clj-tdo is a command line tool for recording tasks."
        ""
        "Usage: clj-tdo [options] action"
        ""
        "Options:"
        options
        ""
        "Actions:"
        "  new          Create a new task."
        "  done         Complete a task."
        "  list [ls]    Show a list of tasks."
        ""
        "Examples:"
        "    Create a new task with title \"My new task.\" and id \"T1\", due on 25/12/2014."
        "      new -t \"My new task.\" -i \"T1\" -d 25/12/2014 "
        ""
        "    Complete the task with id \"T1\""
        "      done -i \"T1\" "
        ""
        "    List all tasks in the \"Work\" category, include finished tasks and sort by Due date."
        "      list -c \"Work\" -f -s D "]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        handlers (all-handlers)]
    (cond
      (:help options) (exit 0 (help summary))
      (not= (count arguments) 1) (exit 1 (help summary))
      errors (exit 1 (error-msg errors)))
    (let [handler ((keyword (first arguments)) handlers) ]
      (if handler
        (handler options (local-disk-store ".clj-tdo"))
        (exit 1 (help summary))))))
