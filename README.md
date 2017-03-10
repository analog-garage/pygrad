# pygrad: a gradle plugin for python tasks
**Version: 0.1.7**  
*Author: Christopher Barber*  
*Last updated: 2017-03-10*

## Introduction

This project provides two simple plugins for performing python tasks in a gradle build:

* `com.analog.garage.pygrad-base` provides a `PythonExtension` type and a number of gradle task classes
* `com.analog.garage.pygrad` creates a `python` extension in the project and adds a number of standard python tasks based on a build-specific python virtual environment.

It is only necessary to apply one of these plugins.

**This project is under development and not yet stable!**

## Requirements

Developed using gradle 3.3. It probably will work with earlier 3.x versions but I have not tried it and don't promise to support earlier versions.

Only supports python 3.4 and later. Tasks may work with earlier versions of python if manually configured. In particular, the implementation of the `PythonVirtualEnvTask` depends on python's venv package introduced in 3.3 and modified in 3.4. I am developing using python 3.5, so there may be other dependencies I have missed.

## Applying the plugin

Add the following to your `build.gradle` script for the full version:

~~~groovy
plugins {
  id 'com.analog.garage.pygrad' version '0.1.7'
}
~~~

or use

~~~groovy
plugins {
  id 'com.analog.garage.pygrad-base' version '0.1.7'
}
~~~

for the base version, which does not modify the project.

## Overview

The full plugin adds the following tasks to the project:

task           | description
-------------- | ---
pyenv          | Sets up project-specific python virtual environment
pyversionfile  | Generates version.py file from gradle project version.
pysources      | Abstract task for dependencies on generated python sources including pyversionfile.
pydevelop      | Installs link to project source in projects python environment.
pytest         | Runs python unit tests.
pycoverage     | Runs python unit tests and generates a coverage data file.
pycoverageHtml | Generates HTML coverage report, depends on pycoverage.
pydoc          | Generates sphinx based python documentation.
pydist         | Builds package distribution files.
pyuploadLocal  | Uploads python package to devpi server on localhost. Depends on pydist.
devpiLogin     | Logs into devpi server using credentials from user's gradle.properties.
artifactoryPublishPython | Uploads python package to artifactory server. Depends on pydist.

These may be configured individually but typically should be configured through the `python` extension. Below is a summary showing the default setting for each property. Projects that use the default directory layout will only need to specify package requirements and extra package repositories.

~~~groovy
python {
   // Base URL of artifactory server.
   artifactoryBaseUrl = null
   
   // Subdirectory of artifactory server holding python package index.
   artifactoryKey = 'python-release-local'
   
   // Full URL to artifactory python package index.
   artifactoryUrl = "$python.artifactoryBaseUrl/$python.artifactoryKey"
   
   // User/password for artifactory uploads. 
   // Should come from user's ~/.gradle/gradle.properties.
   artifactoryUser = "$project.artifactoryUser"
   artifactoryPassword = "$project.artifactoryPassword"
   
   // Base directory for build artifacts
   buildDir = "$project.buildDir/python"
   
   // Python requirements needed for building/testing but not for runtime distribution.
   buildRequirements = []
   
   // Base directory for python coverage data and reports.
   coverageDir = "$python.buildDir/coverage"
   
   // Directory for python HTML coverage report.
   coverageHtmlDir = "$python.coverageDir/html"
   
   // Name of index subdirectory for devpi server
   devpiIndex = "$project.devpiIndex"
   
   // Username/password to use with devpi server. 
   // Should come from user's ~/.gradle/gradle.properties.
   devpiUser = "$project.devpiUser"
   devpiPassword = '$project.devpiPassword'
   
   // HTTP port used by devpi server (3141 is a common value)
   devpiPort = "$project.devpiPort"
   
   // Directory that will contain generated python distribution files.
   distDir = "$python.buildDir/dist"
   
   // Root directory for generated python documentation.
   docsDir = "$python.buildDir/docs"
   
   // Directory containing sphinx documentation source for python.
   docSourceDir = "doc/python-api"
   
   // Name of python package defined by this project.
   packageName = "$project.name"
   
   // Name or path to python executable used to set up project's virtual environment.
   pythonExe = 'python3'
   
   // Additional python package repositories (i.e package index urls) for downloads.
   repositories = []
   
   // Python requirement strings for packages required by this project.
   requirements = []
   
   // Location of setup.py file used for distribution tasks.
   setupFile = '$python.sourceDir/setup.py'
   
   // Root directory for python source code for project.
   sourceDir = "src/main/python3"
   
   // Root directory for python unit test discovery.
   testDir = "$python.sourceDir"
   
   // Location of project-specific python virtual environment.
   venvDir = "$python.buildDir/venv"
   
   // Python package version, based on project version by default.
   version = { project.version.replace('-SNAPSHOT', '.dev0') }
   
   // Location of python package version.py file, if any.
   versionFile = null
}
~~~

All of the list properties may be appended to by dropping the `=` from the syntax. Furthermore, a single package requirement can be added using the `require` method. For example:

~~~groovy
python {
   repositories devpiUrl, artifactoryUrl
   require 'numpy>=0.12'
}
~~~






