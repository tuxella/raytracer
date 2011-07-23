(ns hello.foo.bar)

(defn log [msg]
  (.log js/console msg))

(defn range
  "Returns a lazy seq of nums from start (inclusive) to end
  (exclusive), by step, where start defaults to 0 and step to 1."
  ([end] (range 0 end 1))
  ([start end] (range start end 1))
  ([start end step]
     (loop [accu [], start start, end end, step step]
       (if (>= start end)
	 (seq accu)
	 (recur (conj accu start) (+ start step) end step)
	 )
       )
     )
  )

;;;;;;;;;;;;;;;;;;;;

(defn draw-line
  [ctx x1 y1 x2 y2 color]
  (do
    ((js* "setStrokeStyle") ctx color)
    (. ctx (moveTo x1 y1))
    (. ctx (lineTo x2 y2))
    (. ctx (stroke))
    )
  )

(defn draw-dot
  [ctx x y color]
  (do
    ((js* "setFillStyle") ctx color)
    (. ctx (fillRect x y 1 1))
    )
  )

;;;;;;;;;;;;;;;;;;;;
;; Math Utility functions
(defn square [x] (* x x))

(defn magnitude [p]
  (Math/sqrt (+ (square (nth p 0)) (square (nth p 1)) (square (nth p 2)))))

(defn unit-vector [p]
  (do
  (let [d (magnitude p)]
    (list (/ (nth p 0) d) (/ (nth p 1) d) (/ (nth p 2) d))))
)
(defn point-subtract [p1 p2]
  (do
    (list
     (- (nth p1 0) (nth p2 0))
     (- (nth p1 1) (nth p2 1))
     (- (nth p1 2) (nth p2 2))))
)
(defn distance [p1 p2]
  (magnitude (point-subtract p1 p2)))

(defn minroot [a b c]
  (if (zero? a)
    (/ (- c) b)
    (let [disc (- (square b) (* 4 a c))]
      (if (> disc 0)
        (let [discroot (Math/sqrt disc)]
          (min (/ (+ (- b) discroot) (* 2 a))
               (/ (- (- b) discroot) (* 2 a))))))))

;; Ray tracing bits
(def eye (list 150 150 200))


(defn defsphere [point r c]
  (list c r point))


(def world [(defsphere (list 150 150 -600) 250 0.4)
            (defsphere (list 175 175 -300) 100 0.7)
	    (defsphere (list 200 200 0) 10 0.3)])

(defn sphere-normal [s pt]
  (let [c (nth s 2)]
    (unit-vector (point-subtract c pt))))

(defn sphere-intersect [s pt ray]
  (let [c (nth s 2)
	n (minroot (+ (square (nth ray 0)) (square (nth ray 1)) (square (nth ray 2)))
		   (* 2 (+
			 (* (- (nth pt 0) (nth c 0)) (nth ray 0))
			 (* (- (nth pt 1) (nth c 1)) (nth ray 1))
			 (* (- (nth pt 2) (nth c 2)) (nth ray 2))))
		   (+ (square (- (nth pt 0) (nth c 0)))
		      (square (- (nth pt 1) (nth c 1)))
		      (square (- (nth pt 2) (nth c 2)))
		      (- (square (nth s 1)))))]
    (if n
      (do
	(list (+ (nth pt 0) (* n (nth ray 0)))
	      (+ (nth pt 1) (* n (nth ray 1)))
	      (+ (nth pt 2) (* n (nth ray 2))))))
    )
  )

(defn lambert [s intersection ray]
  (let [normal (sphere-normal s intersection)]
    (max 0 (+ (* (nth ray 0) (nth normal 0))
              (* (nth ray 1) (nth normal 1))
              (* (nth ray 2) (nth normal 2))))))

;; second item = what we hit
;; first item = where we hit
(defn first-hit [pt ray]
  (reduce
   (fn [x y]
     (let [hx (first x) hy (first y)]
       (cond
	(nil? hx) y
	(nil? hy) x
	:else (let [d1 (distance hx pt) d2 (distance hy pt)]
		  (if (< d1 d2) x y)))))
   (map (fn [obj]
	  (let [h (sphere-intersect obj pt ray)]
	    (if (not (nil? h))
	      (do
		[h obj]
		)
		))) world)))

(defn send-ray [src ray]
  (let [hit (first-hit src ray)]
    (if (not (nil? hit))
      (let [int (first hit)
	    s (second hit)]
	(* (lambert s ray int) (nth s 0)))
      0
      )
    )
  )

(defn color-at [x y]
  (let [ray (unit-vector (point-subtract (list x y 0) eye))
	res (send-ray eye ray)]
    res
    )
  )

(defn grayscalify [c]
  (let [n (mod c 255)
	h (.toString n 16)]
    (str "#" h h h)
    )
  )

(defn ray-trace [ctx w h ox oy]
  (do
    (doseq [x (range 0 (dec w))]
      (doseq [y (range 0 (dec h))]
	(let [color (color-at (+ x ox) (+ y oy))]
	  (if (< 0 color)
	    (let [c (js/Math.ceil color)]
	      (draw-dot ctx x y (grayscalify c))
	      )
	    )
	  )
	)
      )
    )
  )

(defn create-work-list [width height unitX unitY]
  (let [xs (range 0 width unitX) ys (range 0 height unitY)]
    (mapcat (fn [x] (mapcat (fn [y] (list (list x y))) ys)) xs)))


(defn ray-trace-cl [ctx w h]
  (fn [pos]
    (ray-trace ctx w h (first pos) (second pos))
    )
  )

(defn draw-trace [ctx]
  (let [width 300
	height 300
	unitX 10
	unitY 10
	work-list (create-work-list width height unitX unitY)]
    (do
      (apply (ray-trace-cl ctx width height work-list))
      )
    )
  )

(defn trace [canvas]
  (let [cvs (nth canvas 0)]
  (do
    (draw-trace (.getContext cvs "2d"))
    )
  )
)
