(ns scrape-reddit.scrape
  (:require [skyscraper :refer :all]
            [net.cgrand.enlive-html :refer :all]
            [alandipert.enduro :as e]))

(def c2 (e/file-atom #{} "resources/comments.clj"))

(defprocessor reddit-user-comments
  :cache-key-fn :url
  :process-fn
  (fn [res ctx]
    (def *c ctx)
    (def *r res)
    (Thread/sleep 20000)
    (let [next-url (-> (select res [:span.nextprev :a]) second :attrs :href)
          comments (->> (select *r [:div.usertext-body :> :div.md :> :p])
                        (mapv (comp first :content)))]
      (println "got new comments for:" (:next-url ctx))
      (e/swap! c2 #(apply conj % comments))
      (if (< 25000 (count @c2))
        ;; no processor => no infinite loop.
        {:next-url next-url
         :comments comments}
        {:next-url next-url
         :comments comments
         :url next-url
         :processor :reddit-user-comments}))))

(defn seed [user-name]
  [{:url (str "https://www.reddit.com/user/"
              user-name
              "?count=1050&after=t3_9vkjz")
    :user user-name
    :processor :reddit-user-comments}])

(comment
  "fire missiles."

  (scrape (seed "user-name") :processed-cache true :html-cache false)




  )
