(ns ui.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce interval-process (atom nil))
(defonce interval-duration (atom 1500))
(defonce duration (atom 0))
(defonce timer-is-active (atom false))
(defonce timer-is-paused (atom false))

(.addEventListener js/document "keyup" (fn [e]
                                         (when (= (.-code e) "Space")
                                           (swap! timer-is-paused not))))

(defn- str->int [s]
  #(js/parseInt s))

(defn calculate-percentage [duration interval-duration]
  (.floor js/Math (* (/ duration interval-duration) 100)))

(defn play-sound [type]
  (cond
    (= type "complete") (let [audio-file (js/Audio. "audio/lick_my_balls.wav")]
                          (set! (.-volume audio-file) 0.1)
                          (.play audio-file))
    :else (js/alert "SOMETHING!")))

(defn stop-timer [interval]
  (.clearInterval js/window interval)
  (reset! duration 0))

(defn start-timer [interval-duration]
  (swap! interval-process
    #(.setInterval js/window
      (fn []
        (if (= @timer-is-paused false)
          (swap! duration inc))
        (when (= @duration interval-duration)
          (stop-timer @interval-process)
          (swap! timer-is-active not)
          (play-sound "complete")))
      1000)))

(defn visor []
  [:div.visor
   {:class
      (when (>= @timer-is-active true)
          "active")
    :style
      {:top (str (calculate-percentage @duration @interval-duration) "%")}}])

(defn pomodoro []
  [:section.pomodoro.ui
    [:h2.time
      [:span (.floor js/Math (/ @duration 60))]
      [:span.smaller-text "m "]
      [:span (mod @duration 60)]
      [:span.smaller-text "s"]]

    [:h2.percentage
      [:span (calculate-percentage @duration @interval-duration)]
      [:span.smaller-text "%"]]])

(defn footer []
  [:footer
    [:div.icn-cog]])

(defn root-component []
  [:div.root
    {:on-click (fn [e]
                 (.preventDefault e)
                 (if (= @timer-is-active false)
                   (do (start-timer @interval-duration)
                       (reset! timer-is-paused false)
                       (swap! timer-is-active not))
                   (do (stop-timer @interval-process)
                       (swap! timer-is-active not))))}
    ; [:canvas#circle-progress
    ;  {:width 220
    ;   :height 220
    ;   :data-progress-amount (calculate-percentage @duration @interval-duration)}]
    [visor]
    [pomodoro]
    [footer]])

(reagent/render
  [root-component]
  (js/document.querySelector ".app-container"))
