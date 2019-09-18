require recipes-bsp/u-boot/u-boot.inc

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=30503fd321432fc713238f582193b78e"

DEPENDS += "dtc-native bison-native"

SRC_URI = " \
    git://git.denx.de/u-boot.git \
    file://0001-am335x_evm.h-console-ttyO0-ttyS0.patch \
    file://0002-Drop-initial-note-as-soon-as-possible.patch \
"
SRCREV = "e5aee22e4be75e75a854ab64503fc80598bc2004"
PV = "v2019.07"
S = "${WORKDIR}/git"

