SUMMARY = "A static C++ library for basic I/O operations on GPIO, SPI, I2C"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=e6a600fd5e1d9cbde2d983680233ad02"
PV = "0.0.0+git${SRCPV}"

inherit autotools

DEPENDS = "dtc-native"

SRC_URI = " \
    git://github.com/piranha32/IOoo.git \
    file://0001-dts-Makefile.am-remove-invalid-target.patch \
    file://0002-Makefile.am-do-not-build-examples.patch \
"
SRCREV = "ce61f3d721bb0897dc9b05243731c22c001b4080"
S = "${WORKDIR}/git"
