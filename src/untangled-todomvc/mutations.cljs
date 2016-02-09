(ns untangled-todomvc.mutations
  (:require [untangled.client.mutations :as m]
            [untangled.client.logging :as log]
            [om.next :as om]))

(defmethod m/mutate 'todo/new-item [{:keys [state]} _ {:keys [text]}]
  {:action (fn []
             (let [id (om/tempid)]
               (swap! state #(-> %
                              (update :todos (fn [todos] ((fnil conj []) todos [:todo/by-id id])))
                              (assoc-in [:todo/by-id id] {:id id :text text})))))})

(defmethod m/mutate 'todo/toggle-complete [{:keys [state]} _ {:keys [id]}]
  {:action (fn []
             (swap! state (fn [st] (update-in st [:todo/by-id id :completed] #(not %))))
             (swap! state (fn [st] (update st :todos/num-completed
                                     (if (get-in st [:todo/by-id id :completed]) inc dec)))))})

(defmethod m/mutate 'todo/toggle-all [{:keys [state]} _ _]
  {:action (fn []
             (let [updated-todos (into {} (map (fn [[id data]] [id (assoc data :completed false)]) (get @state :todo/by-id)))]
               (swap! state #(-> %
                              (assoc :todos/num-completed 0)
                              (assoc :todo/by-id updated-todos)))))})

(defmethod m/mutate 'todo/delete-item [{:keys [state]} _ {:keys [id]}]
  {:action (fn []
             (letfn [(remove-item [todos] (vec (remove #(= id (second %)) todos)))]
               (swap! state #(if (get-in % [:todo/by-id id :completed])
                              (update % :todos/num-completed dec)
                              %))
               (swap! state #(-> %
                              (update :todos remove-item)
                              (update :todo/by-id dissoc id)))))})
