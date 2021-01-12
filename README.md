[![Build Status](https://travis-ci.org/jde-emacs/jde-server.png?branch=master)](https://travis-ci.org/jde-emacs/jde-server)

# jde-server
JDEE Java backend

# Building:
[Maven 3](https://maven.apache.org/) is required to build jde-server. You will need to clone jde-server from git, run the build, copy the jde-bundle.jar to a new directory, and point ```jde-server-dir``` variable in Emacs to the directory containing the jar.

1. Install Maven (if you don't already have it)
2. At the terminal enter the following commands in a directory of your choice:
3. ```$ git clone https://github.com/jde-emacs/jde-server.git```
4. ```$ cd jde-server```
5. ```$ mvn -Dmaven.test.skip=true package```
6. Copy ```target/jde-bundle-${version}.jar``` to a directory of your choice (e.g. ```~/myJars```)
7. Start Emacs and enter the following commands:
8. ```M-x customize```
9. In the search field enter ```jde-server-dir```
10. In the field next to "Jdee Server Dir:" enter the directory holding the jar from step 6 (e.g. ```~/myJars```)
11. Click the "Apply and Save" button
