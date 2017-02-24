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

import static com.analog.garage.pygrad.LazyPropertyUtils.*

import java.io.File

import org.gradle.api.tasks.*

import com.analog.garage.pygrad.PythonTaskBase

/**
 * Base class for python tasks that use the python coverage executable.
 * <p>
 * @author Christopher Barber
 */
class PythonCoverageTaskBase extends PythonModuleTaskBase {

	// --- coverageArgs ---
	
	private List<Object> _coverageArgs = []
	@Input
	List<String> getCoverageArgs() { stringifyList(_coverageArgs) }
	void setCoverageArgs(Object ... args) { 
		_coverageArgs.clear()
		addToListFromVarargs(_coverageArgs, args) 
	}
	void coverageArgs(Object first, Object ... additional) {
		addToListFromVarargs1(_coverageArgs, first, additional)
	}
	
	// --- coverageDir ---
	
	private Object _coverageDir = "$project.buildDir/python/coverage"
	
	@Internal
	File getCoverageDir() { project.file(_coverageDir) }
	void setCoverageDir(Object path) { _coverageDir = path }
	
	// --- coverageFile ---
	
	private Object _coverageFile = 'python.coverage'
	@Internal
	File getCoverageFile() { resolveFile(coverageDir, _coverageFile) }
	void setCoverageFile(Object path) { _coverageFile = path }
	
	//--------------
	// Construction
	//
	
	PythonCoverageTaskBase() {
		super('coverage')
	}
	
	//------
	// Task
	//
	
	@TaskAction
	void runCoverage() {
		project.exec {
			executable = pythonExe
			args = ['-m', module] + coverageArgs
			workingDir = coverageDir
			environment['COVERAGE_FILE'] = coverageFile
		}
	}
}
