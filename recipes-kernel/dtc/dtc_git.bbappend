# to support overlays we compile different sources
# these are mainline master sources and
# + ac7c746b55cc1bc652e3d9f878ecf7d2384a647e fdtdump: Add live tree dump capability
# + 17b9daa69434e1252389658af1326bc6757d4dfb dtc: Plugin and fixup support
# + 4273dca28b716396cefde8027c7b3280a0b9a4b5 dtc: Document the dynamic plugin internals

SRC_URI = "git://github.com/pantoniou/dtc.git;branch=dt-overlays6;"
SRCREV = "4273dca28b716396cefde8027c7b3280a0b9a4b5"

SOLIBSDEV = "fdt.so"
FILES_${PN} += "${libdir}/libfdt-*.so"
