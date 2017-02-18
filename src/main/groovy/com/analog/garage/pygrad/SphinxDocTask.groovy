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

import org.gradle.api.tasks.*

import com.analog.garage.pygrad.PythonTaskBase

/**
 * Task generates python documentation using sphinx-build program
 * <p>
 * @author Christopher Barber
 */
class SphinxDocTask extends PythonTaskBase {

	//------------
	// Properties
	//
	
	// --- builder ---

	private Object _builder = 'html'
	
	/**
	 * Builder to use for generating documentation.
	 * <p>
	 * Default is 'html'
	 * <p>
	 * See sphinx-build documentation for other options.
	 * <p>
	 */
	@Input
	String getBuilder() { stringify(_builder) }
	void setBuilder(Object builder) { _builder = builder }
	
	// --- outputDir ---	
	
	private Object _outputDir = { new File(project.buildDir, 'python/doc/' + builder) }
	
	/**
	 * Directory in which documentation will be generated
	 * <p>
	 * Defaults to python/doc/{@link #getBuilder &lt;builder&gt;} subdirectory
	 * of project's {@code buildDir}
	 */
	@OutputDirectory
	File getOutputDir() { project.file(_outputDir) }
	void setOutputDir(Object path) { _outputDir = path }
	
	// --- sourceDir ---
	
	private Object _sourceDir
	
	/**
	 * Root directory containing source of documentation.
	 */
	@InputDirectory
	File getSourceDir() { project.file(_sourceDir) }
	void setSourceDir(Object path) { _sourceDir = path }
	
	// --- sphinxBuildExe ---
	
	private Object _sphinxExe = null
	
	/**
	 * Name or location of sphinx-build executable.
	 * <p>
	 * Default is {@link PythonVirtualEnvSettings#getSphinxBuildExe sphinxBuildExe} from
	 * {@link #venv} if available, else 'sphinx-build'.
	 */
	@Input
	String getSphinxBuildExe() {
		if (_sphinxExe == null) {
			return venv != null ? venv.sphinxBuildExe : 'sphinx-build'
		}
		return stringify(_sphinxExe) 
	}
	
	void setSphinxBuildExe(Object exe) { _sphinxExe = exe }

	//------
	// Task
	//
	
	@TaskAction
	runSphinx() {
		project.exec {
			executable = sphinxBuildExe
			args = ['-b', builder, sourceDir, outputDir]
		}
	}
}
