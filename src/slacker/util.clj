(ns slacker.util)

(defmacro let-props [bindings & body]
  {:pre [(even? (count bindings))]}
  (let [bindings (partition 2 bindings)]
    `(let [~@(mapcat (fn [[props val]]
                       (mapcat
                        (fn [prop]
                          [prop (list (symbol (str ".-" prop)) val)])
                        props)) bindings)]
       ~@body)))
