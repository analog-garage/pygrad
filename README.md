# pygrad: a gradle plugin for python tasks
**Version: 0.1.10**  
*Author: Christopher Barber*  
*Last updated: 2018-3-9*

## Introduction

This project provides two simple plugins for performing python tasks in a gradle build:

* `com.analog.garage.pygrad-base` provides a `PythonExtension` type and a number of gradle task classes
* `com.analog.garage.pygrad` creates a `python` extension in the project and adds a number of standard python tasks based on a build-specific python virtual environment.

It is only necessary to apply one of these plugins.

**WARNING: I will attempt to maintain backward compatibility with previous releases, but there is no guarantee.**

## Requirements

Developed using gradle 3.3. It probably will work with earlier 3.x versions but I have not tried it and don't promise to support earlier versions.

Only supports python 3.4 and later. Tasks may work with earlier versions of python if manually configured. In particular, the implementation of the `PythonVirtualEnvTask` depends on python's venv package introduced in 3.3 and modified in 3.4. I am developing using python 3.5, so there may be other dependencies I have missed. If conda is used to create environment, then earlier versions of
Python may be used, although not all tasks are guaranteed to work (e.g. the coverage task is not
expected to work under Python 2.7).

## Applying the plugin

Add the following to your `build.gradle` script for the full version:

~~~groovy
plugins {
  id 'com.analog.garage.pygrad' version '0.1.10'
}
~~~

or use `'com.analog.garage.pygrad-base'` for the base version, which only provides the API but does not add tasks or `python` extension to the project.

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
   // Artifactory API key used for authentication in place of user and password
   artifactoryApiKey = null

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
   
   // Path to optional conda YAML environment file
   condaEnvFile = null

   // Name or path of conda executable when useConda is true
   condaExe = 'conda'

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

   // Python version to use when creating conda-based environment.
   pythonVersion = '3.6'
   
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
   
   // Specifies whether to use conda to create virtual environment instead of python3 venv module
   useConda = false

   // Location of project-specific python virtual environment.
   venvDir = "$python.buildDir/venv"
   
   // Python package version, based on project version by default.
   version = { project.version.replace('-SNAPSHOT', '.dev0') }
   
   // Location of python package version.py file, if any.
   versionFile = null
}
~~~

All of the list properties may be appended to by dropping the `=` from the syntax. This Furthermore, a single package requirement can be added using the `require` method. For example:

~~~groovy
python {
   repositories devpiUrl
   require 'numpy>=0.12'
}
~~~

There is also a special `addArtifactoryRepository` method that is disabled when the property `noartifactory` is defined:

~~~groovy
python {
   addArtifactoryRepository
   // equivalent to
   if (!project.hasProperty('noartifactory'))
       repositories artifactoryUrl
}
~~~

This is useful when the artifactory repository is not available temporarily (e.g. when downloading from a laptop outside the firewall).

## Conda Environments

By default, the plugin will create a python virtual environment using the Python 3 `venv` module 
and will install all package requirements using pip. If you would rather use a conda-based environment,
you should enable it using the `useConda` option and specify what version of python you want to base it
on. The conda executable must either be in the search path or be explicitly specified using the `condaExe`
attribute.

~~~groovy
python {
    useConda = true
    pythonVersion = '3.6.1'
    // condaExe = '/usr/local/bin/conda' 
}
~~~

When a conda environment is in use, all package requirements will be installed using `conda install`
but will fallback on `pip install` if the package is not found. If you want to recreate an exact conda
environment, you can save it using:

~~~bash
$ conda env export > my-environment.yml
~~~

check this into your project, and use this file to create the environment in builds:

~~~groovy
python {
    useConda = true
    condaEnvFile = 'my-environment.yml'
}
~~~

## Artifactory hosted PyPi repositories

This plugin supports deployment to Artifactory hosted pypi repositories using [Artifactory's
REST API](https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API). To use it,
you must configure the URL of the repository, the name of the python repository subdirectory,
and authentication credentials for the Artifactory instance. 

Since the configuration is typically
shared across projects and the authentication information is often per-user, it is best to define
these as properties in the user's `gradle.properties` file, which is usually located in the `.gradle/`
subdirectory of the user's home directory. It is advisable to restrict the access permissions on this
file so that other's cannot see the authentication information. To support multiple artifactory configurations,
you can use a string prefix to denote the different configurations. Authentication can be done either via
username/password or through an API key configured on the server. API keys are recommended whenever the password
is shared with other accounts (which may happen implicitly if the server is configured for LDAP).

For instance, given the following settings in a properties file:

~~~ini
# Configuration for internal Artifactory repository
dev.artifactoryUrl=https://artifactory.mycompany.com/artifactory
dev.artifactoryPythonKey=python
dev.artifactoryApiKey=ABCDEF...XYZ
~~~

The `build.gradle` would only need to specify:

~~~groovy
python {
   artifactoryPrefix = 'dev.'
}
~~~

Building the `artifactoryPublishPython` task will upload the outputs of the `pydist` task
to the specified directory on the Artifactory server.




