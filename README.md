# clas12-offline-software
[![Build Status](https://github.com/jeffersonlab/clas12-offline-software/workflows/Coatjava-CI/badge.svg)](https://github.com/jeffersonlab/clas12-offline-software/actions)
[![codecov](https://codecov.io/gh/JeffersonLab/clas12-offline-software/branch/development/graph/badge.svg?precision=2)](https://codecov.io/gh/JeffersonLab/clas12-offline-software/branch/development)


<!--## Quick Start-->

This README fell too far out of date and is undergoing resurrection.  Meanwhile, these bits are still relevant ...

If you just want to use the software without modifying/building it, you can download a pre-built package from the [github releases](https://github.com/JeffersonLab/clas12-offline-software/releases) page or the corresponding repo at [JLab](https://clasweb.jlab.org/clas12offline/distribution/coatjava/).  Builds on JLab machines are also available, see the [general software wiki](https://clasweb.jlab.org/wiki/index.php/CLAS12_Software_Center) for setting up your environment to use them.

For anything more, see the "General Developer Doucmentation" link on that software wiki, which points [here](https://clasweb.jlab.org/wiki/index.php/COATJAVA_Developer_Docs).

The [troubleshooting](https://github.com/JeffersonLab/clas12-offline-software/wiki/Troubleshooting) wiki page may also still be useful but likely outdated.

<!--Javadocs can be found at the repository's [gh-page](https://jeffersonlab.github.io/clas12-offline-software/). A build history can be found at [Travis CI](https://travis-ci.org/JeffersonLab/clas12-offline-software).-->

<!--
## Repository Structure and Dependency Management
### Common Tools
The heart and soul of coatjava is the common tools, or coat-libs, the source code for which can be found in the common-tools subdirectory. coat-libs has 6 modules - clas-utils, clas-physics, clas-io, clas-geometry, clas-detector, and clas-reco - each of which is contained in a subdirectory of common-tools and has the following dependencies. The order of the modules matters and a module can depend on previous modules.

* clas-utils: Apache Commons Math (https://mvnrepository.com/artifact/org.apache.commons/commons-math3)
* clas-physics: none
* clas-io: org.jlab.coda.jevio, org.hep.hipo.hipo, org.jlab.coda.et, org.jlab.coda.xmsg (all from http://clasweb.jlab.org/clas12maven/), and clas-utils
* clas-geometry: ccdb (http://clasweb.jlab.org/clas12maven/)
* clas-detector: clas-utils, clas-io, clas-geometry, org.jlab.groot (http://clasweb.jlab.org/clas12maven/)
* clas-reco: clas-io, clas-physics, clas-utils, clas-detector

(Aside: It would be good to know where the source code is for all of the above dependencies. groot and hipo are currently kept in Gagik's personal github account (gavalian), but he has discussed moving them to the JeffersonLab organization in the future. From within JLab, the clas12maven repo is at /group/clas/www/clasweb/html/clas12maven/org/jlab/coat/)

When build-coatjava.sh runs, it first goes into common-tools and uses Maven to build the coat-libs jar and then creates a new local repository (myLocalMvnRepo) and adds coat-libs to this repository for other parts of the project to use.

Merging of the various reconstruction codes was finished on April 14, 2017. The commit histories were preserved; however, take note of github's method of displaying commit histories: https://help.github.com/articles/differences-between-commit-views/

## some useful links:
http://scottwb.com/blog/2012/07/14/merge-git-repositories-and-preseve-commit-history/ <br>
https://www.smashingmagazine.com/2014/05/moving-git-repository-new-server/ <br>
http://roufid.com/3-ways-to-add-local-jar-to-maven-project/ <br>
http://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-in-maven-project <br>
http://stackoverflow.com/questions/600079/how-do-i-clone-a-subdirectory-only-of-a-git-repository/28039894#28039894

-->

