# clas12-offline-software [![Build Status](https://travis-ci.org/JeffersonLab/class12-offline-software.svg?branch=master)](https://travis-ci.org/JeffersonLab/clas12-offline-software)
CLAS12 Offline Software

common repo for clas12 offline software, merging in progress...
useful links:
http://scottwb.com/blog/2012/07/14/merge-git-repositories-and-preseve-commit-history/
https://www.smashingmagazine.com/2014/05/moving-git-repository-new-server/
http://roufid.com/3-ways-to-add-local-jar-to-maven-project/

sparse checkout: http://stackoverflow.com/questions/600079/how-do-i-clone-a-subdirectory-only-of-a-git-repository/28039894#28039894

stuff to do:

remove jar files from lib/ directory (https://help.github.com/articles/removing-files-from-a-repository-s-history/) these should be built from source, not saved in the repo

get travis working properly with maven

make sure submodule permissions are correct (travis seems to be complaining about it)

consider changing permissions and/or creating teams for development

continue to migrate code
