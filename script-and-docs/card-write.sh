#! /bin/bash

# write-card.sh
# (c) Copyright 2013-2018 Andreas Müller <schnitzeltony@gmail.com>
# Licensed under terms of GPLv2
#
# This script writes all data (MLO / u-boot / kernel / rootfs) to SDCard. To
# select card device and rootfs a dialog based GUI is used.

SelectRootfs() {
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
		for i in `find ${BuildPath}/deploy/images/${MACHINE} -name *.tar.bz2 | sort` ; do
			iCount=`expr $iCount + 1`
			RootFileNameArr[${iCount}]=$i
			strSelection="$strSelection $iCount "`basename $i`
		done
	done

	# were files found?
	if [ $iCount -eq 0 ]; then
		echo "No rootfs files found in ${TMPDIR}-\*"
		exit 1
	fi
	
	dialog --title 'Select rootfs'\
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
	RootFsFile=${RootFileNameArr[$menuitem]}
	echo 
}

run_user() {
	if [ -z $DevicePath ]; then
		# DevicePath for memory card
		SelectCardDevice || exit 1
	fi

	if [ -z $RootFsFile ]; then
		# select rootfs
		SelectRootfs || exit 1
	fi
	RootParams="$DevicePath $RootFsFile $KERNEL_IMAGE_TYPE"
}

run_root() {
	# device node valid?
	if [ ! -b $DevicePath ] ; then
		echo "$DevicePath is not a valid block device!"
		exit 1
	fi
	# rootfs valid?
	if [ ! -e $RootFsFile ] ; then
		echo "$RootFsFile can not be found!"
		exit 1
	fi

	IMAGEDIR=$(dirname $RootFsFile)

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

	# bootloaders and environment
	echo "Writing bootloaders and environment to boot partition"
	mount ${DevicePath}1 /tmp/tmp_mount$$ || exit 1
	rm -rf /tmp/tmp_mount$$/*
	if [ -e ${IMAGEDIR}/MLO-${MACHINE} ] ; then
		cp ${IMAGEDIR}/MLO-${MACHINE} /tmp/tmp_mount$$/MLO
	fi
	cp ${IMAGEDIR}/u-boot-${MACHINE}.img /tmp/tmp_mount$$/u-boot.img
	if [ -e ${IMAGEDIR}/uEnv.txt ] ; then
		cp ${IMAGEDIR}/uEnv.txt /tmp/tmp_mount$$
	fi
	sleep 1
	umount ${DevicePath}1 || exit 1

	# rootfs
	time(
		mount ${DevicePath}2 /tmp/tmp_mount$$ || exit 1
		rm -rf /tmp/tmp_mount$$/*
		cd /tmp/tmp_mount$$
		tar xvjf $RootFsFile
		cd ..
		umount ${DevicePath}2 || exit 1
	)
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
RootFsFile=$2
KernelImageType=$3

# On the 1st call: run user
# After the 2nd call: run root
RootParams='$DevicePath $RootFsFile $KERNEL_IMAGE_TYPE'
chk_root&&run_root



