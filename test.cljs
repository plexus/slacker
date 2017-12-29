(require-macros '[slacker.async :as a])

(a/await [val (a/promise [resolve reject]
                (resolve 42))]
  (println val))
