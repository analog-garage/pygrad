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

import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.util.*

import com.analog.garage.pygrad.base.PythonVirtualEnvSettings

class PythonExtension {

	//------------
	// Properties
	//

	// --- buildDir ---
	
	private Object _buildDir = "${project.buildDir}/python"
	
	/**
	 * Root location for generated python artifacts
	 * <p>
	 * Defaults to 'python/' subdirectory of project's 
	 * {@link Project#getBuildDir buildDir}
	 */
	File getBuildDir() { project.file(_buildDir) }
	void setBuildDir(Object path) { _buildDir = path }

	// --- buildRequirements ---
	
	private List<String> _buildRequirements = ['coverage', 'sphinx']
	
	@Input
	Set<String> getBuildRequirements() { stringifySet(_buildRequirements) }
	void setBuildRequirements(Object ... requirements) {
		_buildRequirements.clear()
		addToListFromVarargs(_buildRequirements, requirements)
	}
	void buildRequirements(Object first, Object ... additional) {
		addToListFromVarargs1(_buildRequirements, first, additional)
	}

	// --- coverageDir ---
	
	private Object _coverageDir = { resolveFile(buildDir, 'coverage') }
	File getCoverageDir() { project.file(_coverageDir) }
	void setCoverageDir(Object path) { _coverageDir = path }

	// --- coverageFile ---
	
	private Object _coverageFile = 'python.coverage'
	File getCoverageFile() { resolveFile(coverageDir, _coverageFile) }
	void setCoverageFile(Object path) { _coverageFile = path }
	
	// --- pythonExe ---

	private Object _pythonExe = 'python3'
	String getPythonExe() { return stringify(_pythonExe) }
	void setPythonExe(String path) { _pythonExe = path}

	private File resolveExe(String path) {
		if (isWindows && !path.toLowerCase().endsWith('.exe'))
			path = path + '.exe'
		return resolveFile(venv.binDir, path)
	}

	def final boolean isWindows
	def final Project project

	private String _curlExe = 'curl'
	File getCurlExe() { resolveExe(_curlExe) }
	void setCurlExe(String path) { _curlExe = path }

	/// --- coverageHtmlDir ---
	
	private String _coverageHtmlDir = 'html'
	File getCoverageHtmlDir() { resolveFile(coverageDir, _coverageHtmlDir) }
	void setCoverageHtmlDir(String path) { _coverageHtmlDir = path }

	private String _docSrcDr = 'doc/python-api'
	File getDocSrcDir() { resolveFile(project.rootDir, _docSrcDr) }
	void setDocSrcDir(String path) { _docSrcDir = path }

	private String _docsDir = 'docs'
	File getDocsDir() { resolveFile(buildDir, _docsDir) }
	void setDocsDir(String path) { _docsDir = path }

	private String _docsHtmlDir = 'html'
	File getDocsHtmlDir() { resolveFile(docsDir, _docsHtmlDir) }
	void setDocsHtmlDir(String path) { _docsHtmlDir = path }

	// --- packageName ---

	private Object _packageName = "$project.name"
	
	/**
	 * The name of the python package.
	 * <p>
	 * Defaults to project name
	 */
	String getPackageName() { stringify(_packageName) }
	void setPackageName(Object name) { _packageName = name }
		
	// --- repositories ---
	
	private List<Object> _repositories = []
	
	/**
	 * Locations of additional Python package repositories
	 */
	@Input
	List<String> getRepositories() { stringifyList(_repositories) }
	void setRepositories(Object ... repositories) {
		_repositories.clear()
		addToListFromVarargs(_repositories, repositories)
	}
	void repositories(Object first, Object ... additional) {
		addToListFromVarargs1(_repositories, first, additional)
	}

	// --- requirements ---
	
	private List<String> _requirements = []
	
	/**
	 * Runtime package requirements.
	 */
	@Input
	Set<String> getRequirements() { stringifySet(_requirements) }
	void setRequirements(Object ... requirements) {
		_requirements.clear()
		addToListFromVarargs(_requirements, requirements)
	}
	void requirements(Object first, Object ... additional) {
		addToListFromVarargs1(_requiremenets, first, additional)
	}
	void require(Object requirement) {
		_requirements.add(requirement)
	}

	// --- setupFile ---
	
	private Object _setupFile = { new File(sourceDir, 'setup.py') }
	File getSetupFile() { project.file(_setupFile) }
	void setSetupFile(Object path) { _setupFile = path }
	
	// --- sourceDir ---
	
	private Object _sourceDir = "$project.rootDir/src/main/python3"
	
	File getSourceDir() { resolveFile(project.rootDir, _sourceDir) }
	void setSourceDir(String path) { _sourceDir = path }

	// --- venv ---
	
	PythonVirtualEnvSettings getVenv() { return new PythonVirtualEnvSettings(venvDir) }
	
	// --- venvDir ---
	
	private Object _venvDir =  { new File(this.buildDir, 'venv') }
	
	/**
	 * Location of python virtual environment.
	 * <p>
	 * Defaults to 'venv/' subdirectory of python {@link #buildDir}
	 */
	File getVenvDir() { project.file(_venvDir) }
	void setVenvDir(String path) { _venvDir = path }

	// -- version --
		
	private Object _version = null
	
	/**
	 * Project version for python release
	 * <p>
	 * If not specified, this defaults to the project version
	 * with "-SNAPSHOT" suffix converted to ".dev0"
	 */
	String getVersion() {
		if (_version != null)
			return stringify(_version)
		else
			return project.version.replace('-SNAPSHOT', '.dev0')
	}
	void setVersion(Object version) { _version = version } 

	//--------------
	// Construction
	//

	PythonExtension(Project project) {
		this.project = project
		isWindows = System.properties['os.name'].toLowerCase().contains('windows')
	}

}
