require recipes-bsp/u-boot/u-boot.inc

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=30503fd321432fc713238f582193b78e"

DEPENDS += "dtc-native bison-native"

SRC_URI = " \
    git://git.denx.de/u-boot.git \
    file://0001-am335x_evm.h-console-ttyO0-ttyS0.patch \
    file://0002-Drop-initial-note-as-soon-as-possible.patch \
"
SRCREV = "0157013f4a4945bbdb70bb4d98d680e0845fd784"
PV = "v2018.11"
S = "${WORKDIR}/git"

