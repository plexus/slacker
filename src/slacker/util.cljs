(ns slacker.util
  (:require [goog.object :as gobj]))

(defn select-props [obj keyseq]
  (into {} (map (fn [k] [k (gobj/get obj (name k))])) keyseq))
