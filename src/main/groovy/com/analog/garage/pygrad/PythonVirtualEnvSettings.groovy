/*------------------------------------------------------------------------
* Copyright 2017 Analog Devices Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*--------------------------------------------------------------------------*/

package com.analog.garage.pygrad

import static LazyPropertyUtils.*

import org.gradle.api.GradleException

/**
 * Interface to contents of a python virtual environment.
 * <p>
 * @author Christopher Barber
 */
class PythonVirtualEnvSettings implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Gradle {@link PythonVirtualEnvTask} responsible for creating and configuring virtual env, if any. 
	 */
	public transient PythonVirtualEnvTask task = null
	
	/**
	 * Root directory of virtual environment.
	 */
	final public File rootDir
	
	
	/**
	 * Subdirectory containing binaries
	 * <p>
	 * On Windows this will be 'Scripts/' subdirectory of {@link #rootDir}, otherwise
	 * this will be 'bin/' subdirectory.
	 */
	final public transient File binDir
	
	/**
	 * Directory containing binaries from python site-packages, if present.
	 */
	final public transient File homeDir

	/**
	 * Location of python executable for this virtual environment.
	 */
	File getPythonExe() { exe('python') }
	
	/**
	 * Location of pip executable for this virtual environment.
	 */
	File getPipExe() { exe('pip') }

	//--------------
	// Construction
	//
	
	PythonVirtualEnvSettings(File rootDir) {
		this.rootDir = rootDir
		binDir = new File(rootDir, isOnWindows() ? "Scripts" : "bin")
		
		// Parse pyvenv.cfg file to find location of site-packages binaries
		def File _homeDir = null 
		def cfgFile = new File(rootDir, 'pyvenv.cfg')
		if (cfgFile.exists()) {
			cfgFile.withInputStream { stream ->
				def props = new Properties()
				props.load(stream)
				def homeStr = props.getProperty('home')
				if (homeStr) {
					_homeDir = new File(homeStr)
				}
			}
		}
		
		homeDir = _homeDir
	}
	
	PythonVirtualEnvSettings(PythonVirtualEnvTask task) {
		this(task.venvDir)
		this.task = task
	}
	
	//---------
	// Methods
	//

	/**
	 * Resolve location of python executable relative to this virtual environment.
	 * <p>
	 * Looks in {@link #binDir} and {@link #homeDir}, if it exists.
	 */
	File exe(String name) {
		def originalName = name
		while (true) {
			def File path = resolveFile(binDir, name)
			if (path.exists())
				return path
			if (homeDir) {
				path = resolveFile(homeDir, name)
				if (path.exists())
					return path
			}
			if (isOnWindows() && !name.contains(".")) {
				name = name + '.exe'
				// Try again with .exe extension
			} else {
				break
			}
		}
		
		throw new GradleException(String.format("Cannot find python executable '%s'", originalName))
	}
}