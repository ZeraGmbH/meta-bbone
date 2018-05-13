#! /bin/bash

# part-card.sh
# (c) Copyright 2013-2018 Andreas Müller <schnitzeltony@gmail.com>
# Licensed under terms of GPLv2
#
# This script prepares partitions on SDCards. It wraps
# http://omappedia.org/wiki/Minimal-FS_SD_Configuration by dialog based GUI.

# for debugging set DEBUG=echo
DEBUG=

run_user() {
	if [ -z $DevicePath ]; then
		SelectCardDevice || exit 1
	fi
	if [ $# -gt 1 ]; then
		echo "Usage: $0 [Card device path] [Rootfs type]"
		exit 1;
	fi
	RootParams="$DevicePath"
}

run_root() {
	# device node valid?
	if [ ! -b $DevicePath ] ; then
		echo "$DevicePath is not a valid block device!"
		exit 1
	fi

	# check if the card is currently mounted
	MOUNTSTR=$(mount | grep $DevicePath)
	if [ -n "$MOUNTSTR" ] ; then
	    echo -e "\n$DevicePath is currenly mounted. Needs unmounting..."
	    $DEBUG umount -f ${DevicePath}?*
	fi

	# kill u-boot environment
	$DEBUG dd if=/dev/zero of=$DevicePath bs=1024 count=1024

	# Create the FAT partition of 64MB and make it bootable
	$DEBUG parted -s $DevicePath mklabel msdos
	$DEBUG parted -s $DevicePath mkpart primary fat32 63s 64MB
	$DEBUG parted -s $DevicePath toggle 1 boot

	# Create the rootfs partition until end of device
	$DEBUG parted -s $DevicePath -- mkpart primary ext4 64MB -0

	# write partitions
	$DEBUG mkfs.vfat -F 32 -n "boot" -I ${DevicePath}1
	$DEBUG mke2fs -F -j -t ext4 -L "rootfs" ${DevicePath}2
}


. `dirname $0`/tools.inc
DevicePath=$1
# On the 1st call: run user
# After the 2nd call: run root
chk_root && run_root
