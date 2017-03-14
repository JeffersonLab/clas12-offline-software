#!/usr/bin/python
import glob
import os

def stringNumber(num):
	value = str(num)
	while len(value)<4:
		value = '0' + value
	return value

files = glob.glob("*.evio")

i = 0
for name in files:
	#print i,name
	newName = "ep_eppi0" + "_A" + stringNumber(i) + "_rec.evio"
	command_t = "$COATJAVA/bin/clara-rec -t 4 -r $COATJAVA/etc/services/debug.yaml " + name + " " + newName
	#os.system(command_t)
	print command_t
	i = i + 1
