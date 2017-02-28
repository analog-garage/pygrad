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

import java.io.File

import org.gradle.api.tasks.*

/**
 * @author Christopher Barber
 */
class DevpiUploadTask extends DevpiTaskBase {
	
	//------------
	// Properties
	//
	
	// --- distDir ---
	
	private Object _distDir = "$project.buildDir/python/dist"
	
	@InputDirectory
	File getDistDir() { project.file(_distDir) }
	void setDistDir(Object path) { _distDir = path }
	
	// --- setupFile ---
	
	protected Object _setupFile = "$project.rootDir/setup.py"
	
	@InputFile
	File getSetupFile() { project.file(_setupFile) }
	void setSetupFile(Object path) { _setupFile = path }
	
	//------
	// Task
	//
	
	@Override
	void runTask() {
		login()
		try {
			use(devpiUrl)
			
			project.exec {
				executable = pythonExe
				args = ['-m', module, 'upload', '--no-vcs', '--from-dir', distDir]
				workingDir = setupFile.parent
			}
		}
		finally {
			logoff()
		}
	}
}
