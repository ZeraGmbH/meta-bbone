#! /bin/bash

# card-kernel-write.sh
# (c) Copyright 2013-2016 Andreas Müller <schnitzeltony@googlemail.com>
# Licensed under terms of GPLv2
#
# This script writes kernel+modules to SDCard. To
# select card device a dialog based GUI is used.

SelectKernel() {
	# OE environment found?
	if [ -z $BUILDDIR ]; then
		echo "The environment variable BUILDDIR is not set. It is usually set before running bitbake."
		exit 1
	fi
	iCount=0
	strSelection=
	for grep_result in `grep -h TMPDIR $BUILDDIR/conf/*.conf | sed -e s/' '/''/g -e s/'\"'/''/g`; do
		# exclude comments
		tmp_dir=`echo $grep_result | grep '^TMPDIR='`
		if [ ! -z $tmp_dir ]; then
			TMPDIR=`echo $tmp_dir | sed -e s/'TMPDIR='/''/g`
		fi
	done
	for BuildPath in ${TMPDIR}-*; do
		for i in `find ${BuildPath}/deploy/images/${MACHINE} -name ${KERNEL_IMAGE_TYPE}'-*-*-*.bin' | sort` ; do
			iCount=`expr $iCount + 1`
			KernelNameArr[${iCount}]=$i
			strSelection="$strSelection $iCount "`basename $i`
		done
	done

	# were files found?
	if [ $iCount -eq 0 ]; then
		echo "No kernel images found in ${TMPDIR}-\*"
		exit 1
	fi

	dialog --title 'Select kernel'\
	--menu 'Move using [UP] [DOWN],[Enter] to select' 30 100 $iCount\
	${strSelection}\
	2>/tmp/menuitem.$$

	# get OK/Cancel
	sel=$?
	# get selected menuitem
	menuitem=`cat /tmp/menuitem.$$`
	rm -f /tmp/menuitem.$$

	# Cancel Button or <ESC>
	if [ $sel -eq 1 -o $sel -eq 255 ] ; then
		echo Cancel selected 1
		return 1
	fi
	KernelImage=${KernelNameArr[$menuitem]}
}

run_user() {
	if [ -z $DevicePath ]; then
		# DevicePath for memory card
		SelectCardDevice || exit 1
	fi

	if [ -z $KernelImage ]; then
		# select rootfs
		SelectKernel || exit 1
	fi
	RootParams="$DevicePath $KernelImage $KERNEL_IMAGE_TYPE"
}

run_root() {
	# device node valid?
	if [ ! -b $DevicePath ] ; then
		echo "$DevicePath is not a valid block device!"
		exit 1
	fi
	# rootfs valid?
	if [ ! -e $KernelImage ] ; then
		echo "$KernelImage can not be found!"
		exit 1
	fi

	IMAGEDIR=$(dirname $KernelImage)

	# check if the card is currently mounted
	MOUNTSTR=$(mount | grep $DevicePath)
	if [ -n "$MOUNTSTR" ] ; then
	    echo -e "\n$DevicePath is currenly mounted. Needs unmounting..."
	    umount -f ${DevicePath}?*
	fi

	# create temp mount path
	if [ ! -d /tmp/tmp_mount$$ ] ; then
		mkdir /tmp/tmp_mount$$
	fi

	# boot partition
	echo "Writing kernel to boot partition"
	mount ${DevicePath}1 /tmp/tmp_mount$$ || exit 1
	cp $KernelImage /tmp/tmp_mount$$/$KernelImageType
	sleep 1
	umount ${DevicePath}1 || exit 1

	# rootfs
	echo "Writing kernel to rootfs"
	mount ${DevicePath}2 /tmp/tmp_mount$$ || exit 1
	cd /tmp/tmp_mount$$/boot
	rm -f [zu]Image*
	# kernel
	KernelFileName=`basename $KernelImage`
	KernelDirName=`dirname  $KernelImage`
        KernelShortFileName=${KernelFileName%*-*-*-*}
	cp $KernelImage $KernelShortFileName
	ln -sf $KernelShortFileName $KernelImageType
	# modules
	echo "Writing modules to rootfs"
	cd ..
	ModulesFileName=`echo $KernelFileName | sed -e s/${KernelImageType}/modules/ -e s/\.bin/\.tgz/`
	ModulesName="${KernelDirName}/${ModulesFileName}"
	tar xvzf $ModulesName lib/modules

	cd ..
	umount ${DevicePath}2 || exit 1

	rm -rf /tmp/tmp_mount$$
}

. `dirname $0`/tools.inc

if [ -z $MACHINE ]; then
	MACHINE=$DEFAULT_MACHINE
fi
if [ -z $KERNEL_IMAGE_TYPE ]; then
	KERNEL_IMAGE_TYPE=$DEFAULT_KERNEL_IMAGE_TYPE
fi

DevicePath=$1
KernelImage=$2
KernelImageType=$3

# On the 1st call: run user
# After the 2nd call: run root
RootParams='$DevicePath $KernelImage $KERNEL_IMAGE_TYPE'
chk_root "The kernel images on %DevicePath% will be overwritten!!" && run_root
