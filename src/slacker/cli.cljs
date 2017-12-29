(ns slacker.cli
  (:require ["fs" :as fs]
            ["@slack/client" :as slack]
            [goog.object :as gobj]
            [lumo.util :as util :refer [file-seq]]
            [clojure.string :as str]
            [slacker.util :refer [select-props]])
  (:require-macros [slacker.async :refer [async await promise]]
                   [slacker.util :refer [let-props]]))

(def client (slack/WebClient. js/process.env.SLACK_API_TOKEN))

(defn slack-get [client type]
  (promise [resolve reject]
    (.list (gobj/get client (name type))
           (fn [err info]
             (println "got" type "yay!")
             (if err
               (reject err)
               (resolve info))))))

(defn slurp-p [file]
  (promise [resolve reject]
    (fs/readFile file (fn [err data]
                        (if err
                          (reject err)
                          (resolve data))))))

(defn channels []
  (await [channels (slack-get client :channels)]
    (into {}
          (comp
           (map #(select-props % [:id :name :created :creator]))
           (map (juxt :id identity)))
          (.-channels channels))))

(defn users []
  (await [users (slack-get client :users)]
    (into {}
          (comp
           (map #(select-props % [:id :name :real_name :is_admin :is_owner :profile]))
           (map (juxt :id identity)))
          (.-members users))))

(defn convert-file! [f users channels]
  (await [contents (slurp-p f)]
    (let [stream (fs/createWriteStream (str "/tmp/slack/" (re-find #"[^\/]*$" f) ".edn"))]
      (.once stream "open"
             (fn [fd]
               (doseq [line (str/split contents #"\n")]
                 (let-props [[text ts user team type channel] (js/JSON.parse line)]
                   (.write stream (prn-str {:type type :text text :ts ts :user (get users user) :channel (get channels channel)}))))
               (.end stream))))

    (catch :default e
      (println "that didn't work" e)
      (println (.-stack e)))))

(defn convert-files! [[f & fs] users channels]
  (await [_ (convert-file! f users channels)]
    (when fs
      (convert-files! fs users channels))))

(defn -main [& args]
  (println "doing the thing")
  (await [channels (channels)
          users (users)]
    (println "got the stuff")
    (fs/writeFile "/tmp/users.edn" (prn-str users))
    (println "users.edn written, yay!")
    (fs/writeFile "/tmp/channels.edn" (prn-str channels))
    (println "done, yay!")
    #_(convert-files! (next (file-seq "/home/arne/github/clojurians-log/logs")) users channels)

    (catch :default e
      (println "that didn't work" e)
      (println (.-stack e)))))
