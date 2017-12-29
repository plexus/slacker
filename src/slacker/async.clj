(ns slacker.async)

(defmacro promise
  "Creates a promise. Takes a binding form which binds the [resolve reject]
  callbacks.

      (promise [res rej]
        (res 42))"
  {:style/indent 1}
  [bindings & body]
  {:pre [(vector? bindings)
         (<= 1 (count bindings) 2)]}
  `(js/Promise. (fn [~@bindings]
                  ~@body)))

(defmacro async
  "Evaluate body inside a promise, return a promise which resolves to the returned
  value. If body throws the promise will reject."
  {:style/indent 0}
  [& body]
  `(js/Promise. (fn [resolve#]
                  (resolve# (do ~@body)))))

(defmacro await
  "Wait for the fulfillment of promises, then run body with the resolved values
  bound. Any top-level catch forms inside body will be used to handle promise
  rejection.

  Consecutive bindings can refer to previous bindings, so you can chain
  resolution of multiple promises.

      (await [page (fetch \"https://clojure.org/\")
              text (.text page)]
        (println (re-find #\"clj-header-message\">.*<\" text))
        (catch :default e
          (println \"Failed to access the network.\"))) "
  {:style/indent 1}
  [bindings & body]
  {:pre [(even? (count bindings))]}
  (let [[[bind-var bind-val] & more-binds] (partition 2 bindings)
        catch? #(and (seq? %) (= 'catch (first %)))
        catches (filter catch? body)
        body (remove catch? body)
        error-sym (gensym 'error)]
    `(-> ~bind-val
         (.then (fn [~bind-var]
                  ~(if more-binds
                     `(await ~(into [] cat more-binds)
                        ~@body)
                     `(do ~@body))))
         (.catch (fn [~error-sym]
                   ~(if (empty? catches)
                      `(throw ~error-sym)
                      `(try
                         (throw ~error-sym)
                         ~@catches)))))))
