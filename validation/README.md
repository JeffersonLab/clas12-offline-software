## Validation

We have 4 levels of validation:

* Unit tests - these are very quick tests (a few seconds) that run automatically with the maven build. Unit tests have a parallel directory structure to the code they are testing. e.g. unit tests for common-tools/clas-physics/src/main/java/org/jlab/clas/physics/LorentzVector.java are located in common-tools/clas-physics/src/test/java/org/jlab/clas/physics/LorentzVectorTest.java (note src/test vs src/main).

* Advanced tests - located here, these tests take a little longer (order of minutes) and have to be manually run by the user when desired. These tests are also run automatically by Travis CI for every change to the repository and for every pull-request.

* Release validation 1 - even more advanced tests that take around an hour. See https://github.com/JeffersonLab/clas12-validation.

* Release validation 2 - even more advanced tests that consists of running relatively large scale simulations and reconstruction; might take many hours. See https://github.com/JeffersonLab/clas12-validation.
