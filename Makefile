##
# Project Title
#
# @file
# @version 0.1

run:
	clojure -M -m fungi.main

nrepl:
	clojure -M:dev -m nrepl.cmdline

format_check:
	clojure -M:format -m cljfmt.main check src

format:
	clojure -M:format -m cljfmt.main fix src

lint:
	clojure -M:lint -m clj-kondo.main --lint .
