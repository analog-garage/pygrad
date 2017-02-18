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

import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.process.ExecSpec

import com.analog.garage.pygrad.PythonExeTaskBase

/**
 * Task to run python setup.py
 * <p>
 * @author Christopher Barber
 */
class PythonSetupTask extends PythonExeTaskBase {

	// --- setupArgs ---
	
	protected final List<Object> _setupArgs = []
	@Input
	List<String> getSetupArgs() { stringifyList(_setupArgs) }
	void setSetupArgs(Object ... args) {
		_setupArgs.clear()
		addToListFromVarargs(_setupArgs, args)
	}
	
	// --- setupFile ---
	
	protected Object _setupFile = "$project.rootDir/setup.py"
	
	@InputFile
	File getSetupFile() { project.file(_setupFile) }
	void setSetupFile(Object path) { _setupFile = path }
	
	// --- workingDir ---
	
	protected Object _workingDir = { setupFile.parent }
	
	@Input
	File getWorkingDir() { project.file(_workingDir) }
	void setWorkingDir(Object path) { _workingDir = path }
	
	//------
	// Task
	//
	
	@TaskAction
	void runSetup() {
		project.exec {
			executable = pythonExe
			args = [setupFile] + setupArgs
			workingDir = this.workingDir
		}
	}
	
	String setupInfo(String type) {
		if (!type.startsWith('-'))
			type = '--' + type
		def out = new ByteArrayOutputStream()
		project.exec {
			executable = pythonExe
			args = [setupFile, type]
			standardOutput = out
		}
		return out.toString().trim()
	}
}
