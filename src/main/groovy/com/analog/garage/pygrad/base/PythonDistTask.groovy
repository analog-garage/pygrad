/**************************************************************************
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
***************************************************************************/

package com.analog.garage.pygrad.base

import static com.analog.garage.pygrad.base.LazyPropertyUtils.*

import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

import com.analog.garage.pygrad.base.PythonSetupTask

/**
 * @author Christopher Barber
 */
class PythonDistTask extends PythonSetupTask {
	
	//------------
	// Properties
	//
	
	// --- distType ---
	
	private Object _distType = 'sdist'
	
	/**
	 * Distribution type
	 * <p>
	 * Defaults to 'sdist' for a source distribution. Other types may include:
	 * <ul>
	 * <li>'bdist' - a binary distribution
	 * <li>'bdist_rpm' - RPM distribution
	 * <li>'bdist_wininst' - MS Windows installer
	 * </ul>
	 * See {@code setup.py --help-commands} for available options.
	 */
	@Input
	String getDistType() { stringify(_distType) }
	void setDistType(Object type) { _distType = type } 
	
	// --- formats ---
	
	private final List<Object> _formats = []
	
	/**
	 * Distribution formats.
	 * <p>
	 * Possible formats will depend on what is available to underlying setup but
	 * may include 'bztar', 'gztar', 'tar', 'xztar', 'zip' (the default), and 'ztar'
	 */
	@Input
	Set<String> getFormats() {
		stringifySet(_formats.isEmpty() ? ['zip'] : _formats) 
	}
	
	void setFormats(Object ... formats) { 
		_formats.clear()
		addToListFromVarargs(_formats, formats)
	}
	
	void setFormat(Object format) {
		setFormats(format)
	}
	
	void formats(Object first, Object ... additional) {
		addToListFromVarargs1(_formats, first, additional)
	}
	
	// --- outputDir ---
	
	private Object _outputDir = "$project.buildDir/python/distributions"
	
	@OutputDirectory
	File getOutputDir() { project.file(_outputDir) }
	void setOutputDir(Object path) { _outputDir = path }
	
	// --- getOutputFiles ---
	
	/**
	 * Distribution output files.
	 * <p>
	 * These will be located in the {@link #getOutputDir outputDir} and will have names
	 * depending on the python package name, version and distribution {@link #getFormats format}.
	 */
	@OutputFiles
	FileCollection getOutputFiles() {
		// Determine fullname of distribution by calling setup
		def out = new ByteArrayOutputStream()
		project.exec {
			executable = pythonExe
			args = [setupFile, '--fullname']
			workingDir = setupFile.parent
			standardOutput = out
		}
		def fullname = (out as String).trim()
		
		def files = []
		for (fmt in formats) {
			files += fullname + extensionForFormat(fmt)
		}
		project.files(files)
	}
	
	//--------------
	// Construction
	//
	
	PythonDistTask() {
		setupArgs = ["$distType", '--dist-dir', "$outputDir", { '--formats=' + formats.iterator().join(',') }]

		doLast {
			// Delete unwanted package.egg-info directory from source tree.
			def name = setupInfo('name')
			def eggInfoDir = new File(setupFile.parent, name + '.egg-info')
			project.delete(eggInfoDir)
		}
	}
	
	
	//------
	// Task
	//
	
	/**
	 * Returns appropriate file extension for given format.
	 */
	static String extensionForFormat(String format) {
		switch (format) {
			case 'bztar': return '.tar.bz'
			case 'gztar': return '.tar.gz'
			case 'tar': return '.tar'
			case 'xztar': return '.tar.xz'
			case 'zip': return '.zip'
			case 'ztar': return '.tar.Z'
		}
		throw new GradleException('Unrecognized distribution format: ' + format)
	}
}
