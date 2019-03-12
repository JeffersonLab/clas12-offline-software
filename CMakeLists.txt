cmake_minimum_required(VERSION 3.10)
project(jacketjava
  VERSION 0.0.1
  DESCRIPTION "clas12 software"
  )

set(CLAS12_INSTALL_DIR ${CMAKE_INSTALL_PREFIX})

configure_file( bin/hipo-utils.in  bin/hipo-utils @ONLY)

install(DIRECTORY coatjava/
  DESTINATION ${CMAKE_INSTALL_PREFIX}
  )

install(FILES bin/hipo-utils
  DESTINATION bin)

