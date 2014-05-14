(ns parser-playground.parser
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s]
            [parser-playground.core :refer [get-resource]]))

(defn select-first [resource sel]
  (-> (html/select resource sel)
      first
      html/text
      s/trim))

;; 1. There is a description where we want it
;; 2. Description is the first paragraph iff there is no <em> element within that paragraph
;; 3. The description is the second paragraph if there is a second paragraph
;; 4. Otherwise, nil
(defn get-description [resource]
  (let [plan1 (select-first resource [:div.zlrecipe-container-border :p.summary.italic])
        plan2 (if (not (s/blank? (select-first resource [:div.post-wrapper :p :em])))
                (-> (html/select resource [:div.post-wrapper :p])
                    second
                    html/text
                    s/trim)
                (select-first resource [:div.post-wrapper :p]))]
    (cond
     (not (s/blank? plan1)) plan1
     (not (s/blank? plan2)) plan2
     :otherwise nil)))

(defn get-ingredient-lines [resource]
  (let [ingredient-list (->> (html/select resource [:div.zlrecipe-container-border :li.ingredient])
                             (map #(s/trim (html/text %))))]
    (map-indexed
     (fn [i txt]
       {:index i :text txt})
     ingredient-list)))

(defn get-image [resource]
  (let [plan1 (-> (html/select resource [:div.post-wrapper :p :img.size-full])
                  last
                  (html/attr-values :src)
                  first)
        plan2 (-> (html/select resource [:div.post-wrapper :div.thumbnail-div :img])
                  first
                  (html/attr-values :src)
                  first)]
    (cond
     (not (s/blank? plan1)) plan1
     (not (s/blank? plan2)) plan2
     :otherwise nil)))

(defn recipe-parser [url]
  (let [resource (get-resource url)
        name (select-first resource [:div.zlrecipe-container-border :div#zlrecipe-title])

        description (get-description resource)
        author (select-first resource [:div.post-info :a])
        recipe-yield (select-first resource
                                   [:div.zlrecipe-container-border :p#zlrecipe-yield :span])
        recipe-yield' (when (-> recipe-yield s/blank? not)
                        recipe-yield)
        image (get-image resource)
        ingredient-lines (get-ingredient-lines resource)]
   {:name name :description description :author author
    :yield_text recipe-yield' :image image
    :ingredient_lines ingredient-lines}))

(comment
  (recipe-parser "http://www.cravingsomethinghealthy.com/pear-prosciutto-and-arugula-pizza/")
  (recipe-parser "http://cravingsomethinghealthy.com/hot-and-sweet-pickled-vegetables/")
  (recipe-parser "http://cravingsomethinghealthy.com/gluten-free-lemon-herb-savory-cheesecake/"))




;; THE OUTPUT
{:name "A recipe" :description "The recipe's description"
 :yield_text "recipe's yield"
 :author "the author"
 :ingredient_lines [{:text "Ingredient text" :index 0}
                    {:text "Ingredient text" :index 1}]
 :image "image url"}
