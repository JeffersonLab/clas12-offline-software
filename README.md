# clas12-offline-software
[![Build Status](https://github.com/jeffersonlab/clas12-offline-software/workflows/Coatjava-CI/badge.svg)](https://github.com/jeffersonlab/clas12-offline-software/actions)
[![codecov](https://codecov.io/gh/JeffersonLab/clas12-offline-software/branch/development/graph/badge.svg?precision=2)](https://codecov.io/gh/JeffersonLab/clas12-offline-software/branch/development)


CLAS12 Offline Software

## Quick Start
If you just want to use the software without modifying/building it, you can download the pre-built package from the [releases](https://github.com/JeffersonLab/clas12-offline-software/releases) page (download coatjava.tar.gz from the Downloads section).

If you plan to use coatjava as a plugin of clara for running CLAS12 reconstruction, you can skip downloading it; installing clara will automatically download coatjava for you. Coatjava can then be found at $CLARA_HOME/plugins/clas12/. See the [clara documentation](https://claraweb.jlab.org/clara/docs/clas/installation.html) for more information.

To build coatjava, your system must have Maven and Java JDK 1.8 or greater installed. Depending on your OS and Java installation, you may also have to install JavaFX (on some systems it will already be installed). If those requirements are met, then to build coatjava simply do:

```tcsh
git clone git@github.com:JeffersonLab/clas12-offline-software.git
cd clas12-offline-softwre
./build-coatjava.sh
```

This will create a new directory called "coatjava" which is your complete coatjava build. Point COATJAVA to this directory:

```tcsh
setenv COATJAVA /path/to/clas12-offline-software/coatjava/
```

See the [troubleshooting](https://github.com/JeffersonLab/clas12-offline-software/wiki/Troubleshooting) wiki page if you experience any issues. Javadocs can be found at the repository's [gh-page](https://jeffersonlab.github.io/clas12-offline-software/). A build history can be found at [Travis CI](https://travis-ci.org/JeffersonLab/clas12-offline-software).

## Repository Structure and Dependency Management
### Common Tools
The heart and soul of coatjava is the common tools, or coat-libs, the source code for which can be found in the common-tools subdirectory. coat-libs has 6 modules - clas-utils, clas-physics, clas-io, clas-geometry, clas-detector, and clas-reco - each of which is contained in a subdirectory of common-tools and has the following dependencies. The order of the modules matters and a module can depend on previous modules.

* clas-utils: Apache Commons  Math (https://mvnrepository.com/artifact/org.apache.commons/commons-math3)
* clas-physics: none
* clas-io: org.jlab.coda.jevio, org.hep.hipo.hipo, org.jlab.coda.et, org.jlab.coda.xmsg (all from http://clasweb.jlab.org/clas12maven/), and clas-utils
* clas-geometry: ccdb (http://clasweb.jlab.org/clas12maven/)
* clas-detector: clas-utils, clas-io, clas-geometry, org.jlab.groot (http://clasweb.jlab.org/clas12maven/)
* clas-reco: clas-io, clas-physics, clas-utils, clas-detector

(Aside: It would be good to know where the source code is for all of the above dependencies. groot and hipo are currently kept in Gagik's personal github account (gavalian), but he has discussed moving them to the JeffersonLab organization in the future. From within JLab, the clas12maven repo is at /group/clas/www/clasweb/html/clas12maven/org/jlab/coat/)

When build-coatjava.sh runs, it first goes into common-tools and uses Maven to build the coat-libs jar and then creates a new local repository (myLocalMvnRepo) and adds coat-libs to this repository for other parts of the project to use.

### CLAS JCSG (Java Constructive Solid Geometry)
A modified version of https://github.com/miho/JCSG. This is the next thing built by build-coatjava.sh (using Gradle). After the build, the jcsg jar is also added to the aforementioned local repository. Andrey is the expert on jcsg.

### Reconstruction
The reconstruction subdirectory contains the reconstruction code for each CLAS12 detector subsystem (in progress). Many of these codes depend on coat-libs, jcsg, and/or other reconstruction codes. build-coatjava.sh goes through each subsystem and builds the reconstruction code, adding the resulting jar files to the aforementioned local repository when necessary. Developers of the reconstruction code should also keep their bank definitions up-to-date inside the etc/bankdefs/ subdirectories. Dependencies are as follows:

* CVT: coat-libs (local repository), org.jlab.coda.jclara (http://clasweb.jlab.org/clas12maven/)
* DC: coat-libs, jcsg (both from local repo), org.jlab.coda.jclara (http://clasweb.jlab.org/clas12maven/)
* TOF: coat-libs, jcsg, dc (all from local repo), org.jlab.coda.jclara (http://clasweb.jlab.org/clas12maven/)

### Other Stuff
After a successful build, you should have a new coatjava/ directory in your working directory which contains all of the CLAS Offline Analysis Tools. In a few cases, the jar files in coatjava/ are simply hard copied from this repository (e.g. lib/clas/* and lib/utils/*). This is probably not a very good practice and will hopefully be fixed in the future.

Merging of the various reconstruction codes was finished on April 14, 2017. The commit histories were preserved; however, take note of github's method of displaying commit histories: https://help.github.com/articles/differences-between-commit-views/

## some useful links:
http://scottwb.com/blog/2012/07/14/merge-git-repositories-and-preseve-commit-history/ <br>
https://www.smashingmagazine.com/2014/05/moving-git-repository-new-server/ <br>
http://roufid.com/3-ways-to-add-local-jar-to-maven-project/ <br>
http://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-in-maven-project <br>

sparse checkout: http://stackoverflow.com/questions/600079/how-do-i-clone-a-subdirectory-only-of-a-git-repository/28039894#28039894
