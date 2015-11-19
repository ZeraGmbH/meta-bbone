RRECOMMENDS_libgl-${PN}_append_bbone = " mesa-megadriver"
RRECOMMENDS_libgles1-${PN}_append_bbone = " mesa-megadriver"
RRECOMMENDS_libgles2-${PN}_append_bbone = " mesa-megadriver"
RRECOMMENDS_libgles3-${PN}_append_bbone = " mesa-megadriver"

# otherwise compiling on target complains for missing gl3 headers
RRECOMMENDS_libgles2-${PN}-dev_append_bbone = " libgles3-${PN}-dev"
