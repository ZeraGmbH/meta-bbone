require recipes-kernel/linux/linux-yocto.inc

DEPENDS += "dtc-native"

SRCREV_machine = "58756fb087cb081259aadf1b6d246584d332f3e7"

SRCREV_dtoverlays = "e4640a24dcd0115bcfb6f491c312de468cd235de"
DESTSUFFIX_DT_OVERLAYS = "dtoverlays"

# linux git repository rebases branch weekly and tags revisions. Therefore
# branch and commit id break and we need to mirror sources @schnitzeltony
SRC_URI = " \
    git://github.com/schnitzeltony/linux.git;name=machine;branch=bb-4.1.13-ti-r36; \
    git://github.com/beagleboard/bb.org-overlays.git;name=dtoverlays;destsuffix=${DESTSUFFIX_DT_OVERLAYS} \
    file://uEnv.txt \
"

LINUX_VERSION = "4.1.13-ti-r36"
LINUX_VERSION_EXTENSION = "-beagleboard.org"
PV = "${LINUX_VERSION}${LINUX_VERSION_EXTENSION}+git${SRCPV}"

COMPATIBLE_MACHINE_bbone = "bbone"
KBUILD_DEFCONFIG_bbone = "bb.org_defconfig"

DTSNAMESFILE = "${B}/dtsnames"

# dt-overlay handling below
# own overlays can be added by appending the following:
#
# <snippet>
# DTS_FILE = "BB-MY-DTS-00A0"
#
# SRC_URI += " \
#     file://${DTS_FILE}.dts \
# "
#
# do_configure_append() {
#     # make our dt-overlay build
#     cp -f "${WORKDIR}/${DTS_FILE}.dts" "${S}/arch/arm/boot/dts/$dtsbase"
#     echo "${DTS_FILE}.dtb" >> ${DTSNAMESFILE}
# }
# </snippet>
#
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
    # dt-overlays
    if [ -e "${DTSNAMESFILE}" ]; then
        cat ${DTSNAMESFILE} | while read dtb; do
            dtbtarget=`basename ${dtb} | sed 's,\.dtb$,.dtbo,g'`
            install ${B}/arch/arm/boot/dts/${dtb} ${D}/lib/firmware/$dtbtarget
        done
    fi

    # ok - uEnv.txt is read by u-boot, but the contents affect kernel so
    # we keep uEnv.txt here not at u-boot.
    install -d ${D}/boot/
    install -m 0755 ${WORKDIR}/uEnv.txt ${D}/boot
}

do_deploy_append() {
    cp ${WORKDIR}/uEnv.txt ${DEPLOY_DIR_IMAGE}
}

FILES_kernel-image += "/boot/uEnv.txt"

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
