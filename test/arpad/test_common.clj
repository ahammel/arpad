(ns arpad.test-common)

(defn close?
  "True if the absolute difference of a and b is within the tolerance (defaults to 0.001"
  ([a b]
   (close? a b 0.001))
  ([a b tollerance]
   (if (or (nil? a) (nil? b))
     false
     (< (Math/abs (- a b)) tollerance))))
