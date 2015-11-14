require recipes-bsp/u-boot/u-boot.inc

DEPENDS += "dtc-native"

SRC_URI += "file://0001-am335x_evm.h-console-ttyO0-ttyS0.patch"
SRCREV = "33711bdd4a4dce942fb5ae85a68899a8357bdd94"
PV = "v2015.07"
