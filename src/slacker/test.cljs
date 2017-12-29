(ns slacker.test
  (:require ["node-fetch" :as fetch])
  (:require-macros [slacker.async :refer [async await promise]]
                   [slacker.util :refer [let-props]]))

;; (defn -main [& args]
(await [page (fetch "https://clojure.org/")
        text (.text page)]
  (println (re-find #"clj-header-message\">.*<" text))
  (catch :default e
    (println "Failed to access the network.")))



;; Pretty basic syntactic sugar macro for creating a promise. Good for when you
;; have some API that takes callbacks, and want to transform it into promises.
(def p
  (promise [resolve reject]
    (callback-based-api (fn [result error]
                          (if error
                            (reject error)
                            (resolve result))))))

;; Async macro which just returns a promise that resolves to the result of the
;; block. Note that due to how promises work you can also return a promise from
;; the block, or if you throw in the block it turns into a rejected promise.
(def num (async
           (inc 41)))

(def num2 (async
            (async (dec 24))))

;; The fun part: wait for multiple promises to resolve, and bind their results
;; to local variables.
(await [n1 num
        n2 num2]
  (println (+ n1 n2)))


;; To handle the rejection of a promise, use `(catch ...)` directly inside the
;; await block. Note also that this is chaining resulting promises. fetch
;; returns a promise, and (.text page) returns a promise based on the result of
;; fetch.
(await [page (fetch "https://clojure.org/")
        text (.text page)
        x (async (th))]
  (println x)
  (catch :default e
    (println "Failed to access the network.")))

;; after
(await [page (fetch "https://clojure.org/")
        text (.text page)]

  ,,,)
