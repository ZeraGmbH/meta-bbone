require recipes-kernel/linux/linux-yocto.inc

DEPENDS += "dtc-native"

KBRANCH = "4.1"
SRCREV_machine_bbone = "a7a1ea5644de091f1a03c29935a5f781c33746ea"

SRCREV_dtoverlays_bbone = "26f9c00e3b85dc145c3ba48afaca03e5ba85c85d"
DESTSUFFIX_DT_OVERLAYS = "dtoverlays"

SRC_URI = " \
    git://github.com/beagleboard/linux.git;name=machine;branch=${KBRANCH}; \
    git://github.com/beagleboard/bb.org-overlays.git;name=dtoverlays;destsuffix=${DESTSUFFIX_DT_OVERLAYS} \
"

LINUX_VERSION = "4.1.9-ti-r20"
LINUX_VERSION_EXTENSION = "-beagleboard.org"
PV = "${LINUX_VERSION}${LINUX_VERSION_EXTENSION}+git${SRCPV}"

COMPATIBLE_MACHINE_bbone = "bbone"
KBUILD_DEFCONFIG_bbone = "bb.org_defconfig"

DTSNAMESFILE = "${B}/dtsnames"

do_configure_append() {
    rm -f ${DTSNAMESFILE}
    for dts in `find ${WORKDIR}/${DESTSUFFIX_DT_OVERLAYS}/src/arm -name '*.dts'`; do
        # copy dt overlays to kernel tree
        dtsbase=`basename $dts`
        echo "Overlay $dtsbase found"
        cp "$dts" "${S}/arch/arm/boot/dts/$dtsbase"

        # keep overlay name
	dtb=`basename $dts | sed 's,\.dts$,.dtb,g'`
        echo $dtb >> ${DTSNAMESFILE}
    done
}

# inspired by oe-core's linux-dtb.inc
do_compile_append() {
    if [ -e "${DTSNAMESFILE}" ]; then
        cat ${DTSNAMESFILE} | while read dtbovl; do
            oe_runmake ${dtbovl}
        done
    fi
}
