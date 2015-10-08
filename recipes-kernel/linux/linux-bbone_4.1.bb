require recipes-kernel/linux/linux-yocto.inc

SRCBRANCH = "4.1"
SRCREV = "a7a1ea5644de091f1a03c29935a5f781c33746ea"

SRC_URI = " \
    git://github.com/beagleboard/linux.git;branch=${SRCBRANCH}; \
"

LINUX_VERSION ?= "4.1.9-ti-r20"

COMPATIBLE_MACHINE_bbone = "bbone"
KBUILD_DEFCONFIG_bbone = "bb.org_defconfig"

