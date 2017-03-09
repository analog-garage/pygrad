# pygrad: a gradle plugin for python tasks
**Version: 0.1.4**  
*Author: Christopher Barber*  
*Last updated: 2017-03-08*

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
  id 'com.analog.garage.pygrad' version '0.1.3'
}
~~~

or use

~~~groovy
plugins {
  id 'com.analog.garage.pygrad-base' version '0.1.3'
}
~~~

for the base version, which does not modify the project.





