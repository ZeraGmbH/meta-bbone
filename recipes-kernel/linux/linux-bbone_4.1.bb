require recipes-kernel/linux/linux-yocto.inc

DEPENDS += "dtc-native"

SRCREV_machine = "8da95c4e7b8c92c2bacd96e051b1c92f744dfaad"

SRCREV_dtoverlays = "26f9c00e3b85dc145c3ba48afaca03e5ba85c85d"
DESTSUFFIX_DT_OVERLAYS = "dtoverlays"

# linux git repository rebases branch weekly and tags revisions. Therefore
# branch and commit id break and we need to mirror sources @schnitzeltony
SRC_URI = " \
    git://github.com/schnitzeltony/linux.git;name=machine;branch=for-meta-bbone; \
    git://github.com/beagleboard/bb.org-overlays.git;name=dtoverlays;destsuffix=${DESTSUFFIX_DT_OVERLAYS} \
"

LINUX_VERSION = "4.1.12-ti-r28"
LINUX_VERSION_EXTENSION = "-beagleboard.org"
PV = "${LINUX_VERSION}${LINUX_VERSION_EXTENSION}+git${SRCPV}"

COMPATIBLE_MACHINE_bbone = "bbone"
KBUILD_DEFCONFIG_bbone = "bb.org_defconfig"

DTSNAMESFILE = "${B}/dtsnames"

# dt-overlay handling below
# TBD initrd required for boot params
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

do_compile_append() {
    if [ -e "${DTSNAMESFILE}" ]; then
        cat ${DTSNAMESFILE} | while read dtb; do
            oe_runmake ${dtb}
        done
    fi
}

do_install_append() {
    if [ -e "${DTSNAMESFILE}" ]; then
        cat ${DTSNAMESFILE} | while read dtb; do
            dtbtarget=`basename ${dtb} | sed 's,\.dtb$,.dtbo,g'`
            install ${B}/arch/arm/boot/dts/${dtb} ${D}/lib/firmware/$dtbtarget
        done
    fi
}

PACKAGES += "kernel-dtoverlays"
FILES_kernel-dtoverlays = ""
ALLOW_EMPTY_kernel-dtoverlays = "1"
DESCRIPTION_kernel-dtoverlays = "Kernel devicetree ovelay meta package"

PACKAGES_DYNAMIC += "^kernel-dtoverlay-.*"

PACKAGESPLITFUNCS_prepend = "split_dt_overlays "

python split_dt_overlays () {
    overlays = do_split_packages(d, root='/lib/firmware', file_regex='^(.*)\.dtbo$', output_pattern='kernel-dtoverlay-%s', description='Devicetree overlay for %s', extra_depends='')
    d.setVar('RDEPENDS_kernel-dtoverlays', ' '.join(overlays))
}
