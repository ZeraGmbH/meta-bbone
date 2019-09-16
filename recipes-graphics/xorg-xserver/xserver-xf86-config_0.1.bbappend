FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_bbone = " \
    file://xorg.conf.d/99-screen.conf \
"

do_install_append_bbone () {
    install -d ${D}/${sysconfdir}/X11/xorg.conf.d/
    install -m 0644 ${WORKDIR}/xorg.conf.d/99-screen.conf ${D}/${sysconfdir}/X11/xorg.conf.d/
}
