#!/usr/bin/env python

"""
    Extracts selected resources from resources directories.

    Useful for grabbing translations from AOSP.

    Point towards base files in the library (in /res/values) and it
    will find all the alternate versions in other directories.
"""

import os
import shutil
from optparse import OptionParser
import xml.etree.ElementTree as ET

ET.register_namespace('android', "http://schemas.android.com/apk/res/android")
ET.register_namespace('xliff', "urn:oasis:names:tc:xliff:document:1.2")

ANDROID_XML_DECLARATION = '<?xml version="1.0" encoding="UTF-8"?>'

def extract_names(path):
	names = {}
	tree = ET.parse(path)

	for child in tree.getroot().iter():
		if 'name' not in child.attrib:
			continue

		names[child.attrib['name']] = None

	return names

def extract(names, res_dir, out_dir):
	# Clear the current output directory
	if os.path.exists(out_dir):
		shutil.rmtree(out_dir)

	os.makedirs(out_dir)

	for root, dirs, files in os.walk(res_dir):
		for file in files:
			path = os.path.join(root, file)
			fileName, fileExtension = os.path.splitext(path)
			if fileExtension == ".xml":
				xml = extract_file(names, path)
				if xml is not None:
					val_dir = os.path.join(out_dir, os.path.relpath(root, res_dir))
					if not os.path.exists(val_dir):
						os.makedirs(val_dir)

					xml.write(os.path.join(val_dir, file),
						encoding = 'utf-8',
						xml_declaration = ANDROID_XML_DECLARATION,
						method = 'xml')

def extract_file(names, path):
	tree = ET.parse(path)
	root = tree.getroot()
	if root.tag != "resources":
		return

	to_remove = []
	found = False
	for child in tree.iter():
		# Only loook at second-level nodes
		if child not in root:
			continue

		if 'name' not in child.attrib or child.attrib['name'] not in names:
			to_remove.append(child)
		else:
			found = True

	for child in to_remove:
		root.remove(child)

	if found:
		return tree

if __name__ == "__main__":
	usage = "usage: %prog [options] baseResFile1 baseResFile2 ..."
	parser = OptionParser(usage=usage)
	parser.add_option('-r', '--res', action="store", help="Resources directory location (/res/)", default="res/")
	parser.add_option('-o', '--out', action="store", help="Output directory", default="out/")
	options, args = parser.parse_args()

	names = {}
	for arg in args:
		names = dict(names.items() + extract_names(arg).items())

	extract(names, options.res, options.out)
