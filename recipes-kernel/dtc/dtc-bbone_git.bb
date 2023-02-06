require recipes-kernel/dtc/dtc.inc

LIC_FILES_CHKSUM = "file://GPL;md5=94d55d512a9ba36caa9b7df079bae19f \
		    file://libfdt/libfdt.h;beginline=3;endline=52;md5=fb360963151f8ec2d6c06b055bcbb68c"


# to support overlays we compile different sources
# these are mainline master sources and
# + ac7c746b55cc1bc652e3d9f878ecf7d2384a647e fdtdump: Add live tree dump capability
# + 17b9daa69434e1252389658af1326bc6757d4dfb dtc: Plugin and fixup support
# + 4273dca28b716396cefde8027c7b3280a0b9a4b5 dtc: Document the dynamic plugin internals

SRC_URI = " \
    git://github.com/pantoniou/dtc.git;branch=dt-overlays6 \
    file://0001-Makefile-align-build-to-avoid-conflicts-with-kernel-.patch \
    file://0002-Fix-build-with-gcc-10.patch \
"
SRCREV = "4273dca28b716396cefde8027c7b3280a0b9a4b5"
PV = "1.4.1+git${SRCPV}"

SOLIBSDEV = "fdt.so"
FILES_${PN} += "${libdir}/libfdt-*.so"

S = "${WORKDIR}/git"

export EXTRAVERSION="-bbone"

do_install_append() {
    mv ${D}${bindir}/dtc ${D}${bindir}/dtc-bbone
    rm -f ${D}${libdir}/libfdt.so*
    rm -f ${D}${libdir}/*.a
}

BBCLASSEXTEND = "native nativesdk"
