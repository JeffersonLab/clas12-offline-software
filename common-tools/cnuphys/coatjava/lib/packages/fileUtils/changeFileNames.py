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
	print i,name
	newName = "gemc_eklambda" + "_A" + stringNumber(i) + "_gen.evio"
	command_mv = "mv " + name + " " + newName
	os.system(command_mv)
	command_t = "$COATJAVA/bin/gemc-evio " + newName + "  10 -1.0 1.0"
	os.system(command_t)
	#print i,command
	i = i + 1
