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

import org.gradle.api.*
import org.gradle.api.tasks.*

import com.analog.garage.pygrad.PythonCoverageTaskBase

/**
 * @author Christopher Barber
 */
class PythonCoverageTask extends PythonCoverageTaskBase {
	
	PythonCoverageTask() {
		description = "Run python unit tests and generate coverage data in .coverage"
	
		coverageArgs = [
			'run', '--omit', '*/site-packages/*', 
			'-m', 'unittest', 'discover', '-s', { -> testDir }
			]
	}
	
	@OutputDirectory
	File getCoverageDir() {
		super.coverageDir
	}
	
	@OutputFile
	File getCoverageFile() {
		super.coverageFile
	}
	
	@InputDirectory
	File getTestDir() {
		super.testDir
	}
	
	@Override
	void runCoverage() {
		coverageDir.mkdirs() // TODO - implement as dependency?
		super.runCoverage()
	}

}
