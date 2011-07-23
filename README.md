# Giants' shoulders
This is a tiny raytracer done in clojure script.
It is based on a port from a Lisp ray tracer found in ANSI Common Lisp
by Paul Graham.

You can find the Clojure version [here](http://www.fatvat.co.uk/2009/01/ray-tracing-in-clojure.html)

# How to use
You need to install clojure-script, then you can compile the project with

	cljsc src/ > hello.js
This will build the development version, accessible with hello-dev.html

# Differences from the Clojure version
Some adaptations were made to replace the Swing UI by an HTML Canvas.

It seems range isn't available in clojure-script, thus I made a pretty ineficient
drop-in replacement.

Clojurescript doesn't support structs, thus I replaced it with
lists. Maps may have been a better substitute however.


# Motivations
This was done to learn some clojure and especially with its brand new
backend: clojurescript.
