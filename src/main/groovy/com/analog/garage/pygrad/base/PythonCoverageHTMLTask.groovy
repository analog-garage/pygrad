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

import java.io.File
import org.gradle.api.tasks.*

import com.analog.garage.pygrad.base.PythonCoverageTaskBase

/**
 * @author Christopher Barber
 */
class PythonCoverageHTMLTask extends PythonCoverageTaskBase {

	// --- outputDir ---
	
	private Object _outputDir = 'html'
	
	@OutputDirectory
	File getOutputDir() { resolveFile(coverageDir, _outputDir) }
	void setOutputDir(Object path) { _outputDir = path }

	// --- title ---
	
	private Object _title = 'Python Coverage'
	@Input
	String getTitle() { stringify(_title) }
	void setTitle(Object newTitle) { _title = newTitle }
	
	//--------------
	// Construction
	//
	
	PythonCoverageHTMLTask() {
		description = 'Generates HTML report of Python test coverage'
		
		coverageArgs = ['html', '-d', { -> outputDir }, '--title', { -> title }]
	}
	
}
