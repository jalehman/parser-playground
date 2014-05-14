(ns parser-playground.core
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s]))

(defn get-resource
  "Given a url, fetches the HTML content and transforms it into an Enlive resource."
  [url]
  (let [user-agent "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/33.0.1750.152 Chrome/33.0.1750.152 Safari/537.36"]
    (try
      (with-open [inputstream (-> (java.net.URL. url)
                                  .openConnection
                                  (doto (.setRequestProperty "User-Agent" user-agent))
                                  .getContent)]
        (html/html-resource inputstream))
      (catch Exception e
        (println (format "URL %s is not a valid html resource." url)))
      (finally
       nil))))

;; The url of the recipe we're working with
(def recipe-url "http://www.cravingsomethinghealthy.com/pear-prosciutto-and-arugula-pizza/")
;; Url with yield
(def recipe-url-with-yield "http://cravingsomethinghealthy.com/montasio-sweet-onion-tart/")
;; Url with Description
(def recipe-url-with-description "http://cravingsomethinghealthy.com/hot-and-sweet-pickled-vegetables/")


;; The Enlive "resource" that we'll work with
(def recipe-resource (get-resource recipe-url))
(def recipe-resource-with-yield (get-resource recipe-url-with-yield))
(def recipe-resource-with-description (get-resource recipe-url-with-description))

;; Let's get just the title of the recipe
;; (html/text (first (html/select recipe-resource [:div#zlrecipe-title])))
;; (With threading operator)
(def recipe-title (-> (html/select recipe-resource [:div.zlrecipe-container-border :div#zlrecipe-title]) first html/text))

;; The title has \n and \t characters in it. We don't need those -- let's get rid of them.
(s/trim recipe-title)

;; The image url
(-> (html/select recipe-resource [:div.zlrecipe-container-border :img.photo])
    first
    (html/attr-values :src)
    first)

;; Ingredients

(def ingredients-elements (html/select recipe-resource [:div.zlrecipe-container-border :li.ingredient]))

;; Four ways to map over the ingredients elements
(map s/trim (map html/text ingredients-elements))

(->> ingredients-elements (map html/text) (map s/trim))

(map (fn [e] (s/trim (html/text e))) ingredients-elements)

(map #(s/trim (html/text %)) ingredients-elements)

;; Author

(-> (html/select recipe-resource [:div.post-info :a]) first html/text s/trim)

;; Tags?


;; Description?
(-> (html/select recipe-resource-with-description [:div.zlrecipe-container-border :p.summary.italic])
    first html/text s/trim)

;; Yield?

(-> (html/select recipe-resource-with-yield [:div.zlrecipe-container-border :p#zlrecipe-yield :span])
    first html/text s/trim)



