/*------------------------------------------------------------------------
* Copyright 2017-2018 Analog Devices Inc.
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
import java.util.List

import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.util.*

import com.analog.garage.pygrad.PythonVirtualEnvSettings

/**
 * Contains project-wide defaults for python tasks.
 * <p>
 * The 'com.analog.garage.pygrad' plugin configures an instance of this
 * under the name 'python' in the {@link Project}'s extensions.
 * <p>
 * Unless otherwise specified all properties are evaluated lazily
 * by get* methods and can be given closure or {@link Callable} values
 * that will return the real value when evaluated. {@link String} and
 * {@link File} properties can also be configured using Groovy
 * GString values (e.g. "$project.someProperty"). File paths are
 * specified relative to the project root unless otherwise specified
 * below.
 */
class PythonExtension {

	//------------
	// Properties
	//

	// --- apiKey ---
	
	private Object _artifactoryApiKey = null
	
	/**
	 * API key for use in {@code artifactoryPublishPython} task.
	 * <p>
	 * If specified, this will be used in place of user/password authentication.
	 * <p>
	 * If not specified explicitly, its value will be taken from the property
	 * with name {@link #artifactoryPrefix} + {@code 'artifactoryApiKey'}, 
	 * which typically should be defined in the user's {@code gradle.properties} file.
	 * <p>
	 * @since 0.1.9
	 */
	String getArtifactoryApiKey() { 
		if (_artifactoryApiKey != null)
			return stringify(_artifactoryApiKey)
		
		return getArtifactoryProperty('artifactoryApiKey')		
	}
	
	void setArtifactoryApiKey(Object key) { _artifactoryApiKey = key }
	
	// --- artifactoryBaseUrl ---
	
	private Object _artifactoryBaseUrl = null
	
	/**
	 * Base URL of artifactory server for publishing packages.
	 * <p>
	 * This is the root directory for all repositories on the artifactory server,
	 * not just the python repository.
	 * <p>
	 * If not set explicitly, returns value of
	 * property with name {@link #artifactoryPrefix} + {@code 'artifactoryUrl'},
	 * which typically should be defined in the user or project {@code gradle.properties} file.
	 */
	String getArtifactoryBaseUrl() {
		if (_artifactoryBaseUrl != null)
			return stringify(_artifactoryBaseUrl)
		
		return getArtifactoryProperty('artifactoryUrl')
	}
	
	/**
	 * Sets {@link #getArtifactoryBaseUrl artifactoryBaseUrl}
	 */
	void setArtifactoryBaseUrl(Object path) { _artifactoryBaseUrl = path }
	
	// --- artifactoryKey
	
	private Object _artifactoryKey = null

	/**
	 * Artifactory key for the python repository on artifactory server.
	 * <p>
	 * This is the subdirectory of the {@link #getArtifactoryBaseUrl artifactoryBaseUrl}
	 * that is the root of the python package repository.
	 * <p>
	 * If not set explicitly, returns value of property with name
	 * {@link #artifactoryPrefix} + {@code 'artifactoryPythonKey'}.
	 * <p>
	 * Defaults to 'python-release-local'
	 */
	String getArtifactoryKey() { 
		String key = getArtifactoryProperty('artifactoryPythonKey')
		
		if (key != null)
			return key
			
		key = stringify(_artifactoryKey) 
		if (key != null)
			return key
			
		return 'python-release-local'
	}
	
	/**
	 * Sets {@link #getArtifactoryKey artifactoryKey}
	 */
	void setArtifactoryKey(Object key) { _artifactoryKey = key }
	
	// --- artifactoryPrefix ---
	
	private Object _artifactoryPrefix = ''
	
	/**
	 * Prefix for artifactory related global properties.
	 * <p>
	 * This is prepended to the property names used by {@link #artifactoryUser},
	 * {@link #artifactoryPassword}, {@link #artifactoryPythonKey} and {@link #artifactoryApiKey}.
	 * It can be used to support multiple artifactory configurations with different 
	 * authentication information.
	 * <p>
	 * The default is the empty string ''.
	 * <p>
	 * @since 0.1.9
	 */
	String getArtifactoryPrefix() { stringify(_artifactoryPrefix) }
	void setArtifactoryPrefix(Object prefix) { _artifactoryPrefix = prefix }
	
	// --- artifactoryUrl ---
	
	private Object _artifactoryUrl = null
	
	/**
	 * URL of the artifactory python package repository.
	 * <p>
	 * Default is "{@link #getArtifactoryBaseUrl &lt;artifactoryBaseUrl&gt;}/{@link 
	 * #getArtifactoryKey &lt;artifactoryKey&gt;}". Returns null if either component
	 * is null.
	 */
	String getArtifactoryUrl() {
		if (_artifactoryUrl != null)
			return stringify(_artifactoryUrl)
	
		def url = artifactoryBaseUrl
		def key = artifactoryKey

		return (url != null && key != null) ? url + '/' + key : null
	}
	
	/**
	 * Sets {@link #getArtifactoryUrl artifactoryUrl}
	 */
	void setArtifactoryUrl(Object path) { _artifactoryUrl = path }
	
	// --- artifactoryUser ---
	
	private Object _artifactoryUser = null
	
	/**
	 * Username for publishing to artifactory repository.
	 * <p>
	 * If not specified explicitly, its value will be taken from the property
	 * with name {@link #artifactoryPrefix} + {@code 'artifactoryUser'}, 
	 * which typically should be defined in the user's {@code gradle.properties} file.
	 */
	String getArtifactoryUser() { 
		if (_artifactoryUser != null)
			return stringify(_artifactoryUser)
		
		return getArtifactoryProperty('artifactoryUser')
	}
	
	/**
	 * Sets {@link #getArtifactoryUser artifactoryUser}
	 */
	void setArtifactoryUser(Object user) { _artifactoryUser = user }

	// --- artifactoryPassword ---
	
	private Object _artifactoryPassword = null
	
	/**
	 * Password for publishing to artifactory repository.
	 * <p>
	 * <p>
	 * If not specified explicitly, its value will be taken from the property
	 * with name {@link #artifactoryPrefix} + {@code 'artifactoryPassword'}, 
	 * which typically should be defined in the user's {@code gradle.properties} file.
	 * <p>
	 * It is usually better to use an API key for authentication instead of user/password,
	 * especially if the server supports LDAP and the password is not specific to Artifactory.
	 * @see #artifactoryApiKey
	 */
	String getArtifactoryPassword() {
		if (_artifactoryPassword != null)
			return stringify(_artifactoryPassword)
			
		return getArtifactoryProperty('artifactoryPassword')
	}
	
	/**
	 * Sets {@link #getArtifactoryPassword artifactoryPassword}
	 */
	void setArtifactoryPassword(Object password) { _artifactoryPassword = password }
		
	// --- buildDir ---
	
	private Object _buildDir = "${project.buildDir}/python"
	
	/**
	 * Root location for generated python artifacts
	 * <p>
	 * Defaults to 'python/' subdirectory of project's 
	 * {@link Project#getBuildDir buildDir}
	 * @see #getRequirements requirements
	 */
	File getBuildDir() { project.file(_buildDir) }
	
	/**
	 * Sets python {@link #getBuildDir buildDir}
	 */
	void setBuildDir(Object path) { _buildDir = path }

	// --- buildRequirements ---
	
	private List<String> _buildRequirements = []
	
	/**
	 * Python package requirements needed for building and testing.
	 * <p>
	 * Arguments should evaluate to a python package requirements
	 * specifier as would be passed to {@code pip}.
	 */
	List<String> getBuildRequirements() { stringifyList(_buildRequirements) }
	
	/**
	 * Sets {@link #getBuildRequirements buildRequirements}.
	 */
	void setBuildRequirements(Object ... requirements) {
		_buildRequirements.clear()
		addToListFromVarargs(_buildRequirements, requirements)
	}
	
	/**
	 * Adds values to {@link #getBuildRequirements buildRequirements}
	 */
	void buildRequirements(Object first, Object ... additional) {
		addToListFromVarargs1(_buildRequirements, first, additional)
	}

	// --- condaEnvFile ---
	
	private Object _condaEnvFile = null
	
	/**
	 * When using conda, create environment from specified environment file.
	 * <p>
	 * This is only used for the initial environment creation. Any additional
	 * requirements will be added afterwards. If null, no environment file
	 * will be used.
	 * <p>
	 * Ignored if {@link #useConda} is false.
	 * 
	 * @since 0.1.9
	 */
	File getCondaEnvFile() { _condaEnvFile == null ? null : project.file(_condaEnvFile) }
	void setCondaEnvFile(Object path) { _condaEnvFile = path }
	
	// --- condaExe ---
	
	private Object _condaExe = 'conda'
	
	/**
	 * Name or path of conda executable.
	 * <p>
	 * This refers to the executable that will be used to configure a python
	 * virtual environment. It must either be an absolute path or be on the system
	 * executable path. This will only be used if the venv task is configured to
	 * use Conda.
	 * <p>
	 * Defaults to 'conda'.
	 * 
	 * @since 0.1.9
	 */
	String getCondaExe() { return stringify(_condaExe) }
	
	/**
	 * Sets {@link #getCondaExe condaExe}
	 */
	void setCondaExe(Object path) { _condaExe = path}

	// --- coverageDir ---
	
	private Object _coverageDir = { resolveFile(buildDir, 'coverage') }
	
	/**
	 * Root build directory for storing results of python test coverage runs.
	 * <p>
	 * Defaults to 'coverage/' subdirectory of python {@link getBuildDir buildDir}.
	 */
	File getCoverageDir() { project.file(_coverageDir) }
	
	/**
	 * Sets {@link #getCoverageDir coverageDir}
	 */
	void setCoverageDir(Object path) { _coverageDir = path }

	// --- coverageFile ---
	
	private Object _coverageFile = 'python.coverage'
	
	/**
	 * Location of python coverage results file.
	 * <p>
	 * Defaults to 'python.coverage' in python {@link #getCoverageDir coveragedir}.
	 */
	File getCoverageFile() { resolveFile(coverageDir, _coverageFile) }
	
	/**
	 * Sets {@link #getCoverageFile coverageFile}
	 * <p>
	 * @param path either evaluates to an absolute path of the coverage file, or a path relative
	 * to the python {@link #getCoverageDir coverageDir}.
	 */
	void setCoverageFile(Object path) { _coverageFile = path }
	
	/// --- coverageHtmlDir ---
	
	private Object _coverageHtmlDir = 'html'
	
	/**
	 * Directory containing generated python html coverage report.
	 * <p>
	 * Defaults to 'html/' subdiretory of python {@link #getCoverageDir coverageDir}.
	 */
	File getCoverageHtmlDir() { resolveFile(coverageDir, _coverageHtmlDir) }
	
	/**
	 * Sets {@link #getCoverageHtmlDir coverageHtmlDir}.
	 * 
	 * @param path either evaluates to an absolute path or else a path relative to the
	 * python {@link #getCoverageDir coverageDir}.
	 */
	void setCoverageHtmlDir(Object path) { _coverageHtmlDir = path }

	// --- devpiIndex ---
	
	private Object _devpiIndex = null
	/**
	 * Name of devpi index subdirectory.
	 * <p>
	 * Defaults to value of {@code devpiIndex} extra project property, or else
	 * 'dev' if there is no such property.
	 */
	String getDevpiIndex() { stringifyWithDefaults(_devpiIndex, project.ext, 'devpiIndex', 'dev') }

	/**
	 * Sets {@link #getDevpiIndex devpiIndex}
	 */
	void setDevpiIndex(Object index) { _devpiIndex = index }
	
	// --- devpiPassword ---
	
	private Object _devpiPassword = null
	
	/**
	 * Password to use when uploading distribution to local devpi server
	 * <p>
	 * Defaults to value of {@code devpiPassword} extra project property,
	 * or else null if there is no such property. Typically this should
	 * be configured in the user's {@code ~/.gradle/gradle.properties} file.
	 * 
	 * @see #getDevpiUser devpiUser
	 */
	String getDevpiPassword() { stringifyWithDefaults(_devpiPassword, project.ext, 'devpiPassword', null) }

	/**
	 * Sets {@link #getDevpiPassword devpiPassword}
	 */
	void setDevpiPassword(Object pwd) { _devpiPassword = pwd }
	
	// --- devpiPort ---
	
	private Object _devpiPort = null
	
	/**
	 * HTTP port of local devpi server.
	 * <p>
	 * Defaults to value of {@code devpiPort{} extra project property,
	 * or else null if there is no such property. Typically this should
	 * be configured in the user's {@code ~/.gradle/gradle.properties} file.
	 * 3141 is a popular value for this port.
	 */
	String getDevpiPort() { stringifyWithDefaults(_devpiPort, project.ext, 'devpiPort', null) }
	
	/**
	 * Sets {@link #getDevpiPort devpiPort}
	 */
	void setDevpiPort(Object port) { _devpiPort = port }

	// --- devpiUrl ---
	
	/**
	 * URL of devpi package index.
	 * <p>
	 * This will be:
	 * <p>
	 * "http://localhost:{@link #getDevpiPort &lt;devpiPort&gt}/{@link 
	 * #getDevpiUser &lt;devpiUser&gt;}/{@link
	 *  #getDevpiIndex &lt;devpiIndex&gt;}"
	 *  <p>
	 *  as long as the component properties are non-null, otherwise this will be null.
	 */
	String getDevpiUrl() {
		def port = devpiPort
		def user = devpiUser
		def index = devpiIndex
		(port && user && index) ? 'http://localhost:' + port + '/' + user + '/' + index : null
	}

	// --- devpiUser ---
	
	private Object _devpiUser = null

	/**
	 * User to use when uploading distribution to local devpi server
	 * <p>
	 * Defaults to value of {@code devpiUser} extra project property,
	 * or else null if there is no such property. Typically this should
	 * be configured in the user's {@code ~/.gradle/gradle.properties} file.
	 * 
	 * @see #getDevpiPassword devpiPassword
	 */
	String getDevpiUser() { stringifyWithDefaults(_devpiUser, project.ext, 'devpiUser', null) }

	/**
	 * Sets {@link #getDevpiUser devpiUser}
	 */
	void setDevpiUser(Object user) { _devpiUser = user }
	
	// --- distDir ---
	
	private Object _distDir = { resolveFile(buildDir, "dist") }
	
	/**
	 * Directory for generated python distribution files
	 * <p>
	 * Defaults to 'dist/' subdirectory of python {@link #getBuildDir buildDir}.
	 */
	File getDistDir() { project.file(_distDir) }
	
	/**
	 * Sets {@link #getDistDir distDir}
	 * 
	 * @param path either an absolute path or will be interepreted relative to
	 * project's {@link Project#rootDir rootDir}.
	 */
	void setDistDir(Object path) { _distDir = path }

	// --- docsDir ---
	
	private Object _docsDir = 'docs'

	/**
	 * Base directory for generated documentation.
	 * <p>
	 * Defaults to 'docs/' subdirectory of python {@link #getBuildDir buildDir}.
	 */
	File getDocsDir() { resolveFile(buildDir, _docsDir) }
	
	/**
	 * Set {@link #getDocsDir docsDir}.
	 * <p>
	 * @param path may be absolute or relative to the python {@link #getBuildDir buildDir}.
	 */
	void setDocsDir(Object path) { _docsDir = path }

	// --- docSourceDir ---
	
	private Object _docSrcDir = 'doc/python-api'
	
	/**
	 * Directory containing python documentation source.
	 * <p>
	 * Default is 'doc/python-api/' subdirectory of project {@link Project#rootDir rootDir}.
	 */
	File getDocSourceDir() { project.file(_docSrcDir) }
	
	/**
	 * Sets {@link #getDocSourceDir docSourceDir}
	 */
	void setDocSourceDir(Object path) { _docSrcDir = path }

	// --- packageName ---

	private Object _packageName = "$project.name"
	
	/**
	 * The name of the python package.
	 * <p>
	 * Defaults to project name
	 */
	String getPackageName() { stringify(_packageName) }
	
	/**
	 * Sets {@link #getPackageName packageName}
	 */
	void setPackageName(Object name) { _packageName = name }
		
	// --- project ---
	
	/**
	 * The project containing this extension.
	 */
	def final Project project

	// --- pythonExe ---

	private Object _pythonExe = 'python3'
	
	/**
	 * Name or path of python executable.
	 * <p>
	 * This refers to the executable that will be used to configure a python
	 * virtual environment. It must either be an absolute path or be on the system
	 * executable path. For full support by this plugin, it must refer to python 3.4
	 * or later.
	 * <p>
	 * Defaults to 'python3'.
	 */
	String getPythonExe() { return stringify(_pythonExe) }
	
	/**
	 * Sets {@link #getPythonExe pythonExe}
	 */
	void setPythonExe(Object path) { _pythonExe = path}

	// -- pythonVersion ---
	
	private Object _pythonVersion = '3.6'
	
	/**
	 * Version of python to use in environment when using conda.
	 * <p>
	 * This is ignored when using standard python 3 virtual-env (which is the default).
	 * <p>
	 * Default is '3.6'.
	 * 
	 * @since 0.1.9
	 */
	String getPythonVersion() { stringify(_pythonVersion) }
	void setPythonVersion(Object version) { _pythonVersion = version }
	
	// --- repositories ---
	
	private List<Object> _repositories = []
	
	/**
	 * URLs of additional python package repositories
	 * <p>
	 * These will be passed to {@code pip} using {@code --extra-index-url} flag.
	 */
	@Input
	List<String> getRepositories() { stringifyList(_repositories) }
	
	/**
	 * Sets {@link #getRepositories repositories}
	 */
	void setRepositories(Object ... repositories) {
		_repositories.clear()
		addToListFromVarargs(_repositories, repositories)
	}
	
	/**
	 * Adds to python {@link #getRepositories repositories}
	 */
	void repositories(Object first, Object ... additional) {
		addToListFromVarargs1(_repositories, first, additional)
	}

	// --- requirements ---
	
	private List<Object> _requirements = []
	
	/**
	 * Runtime package requirements.
	 * 
	 * @see #getBuildRequirements buildRequirements
	 */
	@Input
	List<String> getRequirements() { stringifyList(_requirements) }
	
	/**
	 * Set runtime python package {@link #getRequirements requirements}
	 */
	void setRequirements(Object ... requirements) {
		_requirements.clear()
		addToListFromVarargs(_requirements, requirements)
	}
	
	/**
	 * Adds to runtime python package {@link #getRequirements requirements} 
	 */
	void requirements(Object first, Object ... additional) {
		addToListFromVarargs1(_requirements, first, additional)
	}
	
	/**
	 * Adds a single package to runtime python package {@link #getRequirements requirements}
	 */
	void require(Object requirement) {
		_requirements.add(requirement)
	}

	// --- setupFile ---
	
	private Object _setupFile = { new File(sourceDir, 'setup.py') }
	
	/**
	 * Location of 'setup.py' file used for building distributions and installation.
	 * <p>
	 * Default's to 'setup.py' in python {@link #getSourceDir sourceDir}. 
	 */
	File getSetupFile() { project.file(_setupFile) }
	
	/**
	 * Sets {@link #getSetupFile setupFile} location.
	 */
	void setSetupFile(Object path) { _setupFile = path }
	
	// --- sourceDir ---
	
	// FIXME - is this really the right default?
	private Object _sourceDir = "$project.rootDir/src/main/python3"
	
	/**
	 * Root directory for python sources.
	 * <p>
	 * Defaults to "src/main/python3"
	 */
	File getSourceDir() { project.file(_sourceDir) }
	
	/**
	 * Sets {@link #getSourceDir sourceDir}
	 */
	void setSourceDir(Object path) { _sourceDir = path }
	
	// --- sourceFiles ---
	
	private List<Object> _sourceFiles = 
		[{ project.fileTree(dir: sourceDir, include: '**/*.py', 
			exclude: sourceDir.toPath().relativize(project.buildDir.toPath()).toString() + '/**')}]
	
	/**
	 * Collection of all python source files.
	 * <p>
	 * Defaults to all files with extension '.py' in tree rooted at {@link #getSourceDir sourceDir}.
	 * <p>
	 * Used as an input dependency for tasks.
	 */
	FileCollection getSourceFiles() {
		project.files(_sourceFiles)
	}
	
	/**
	 * Sets {@link #getSourceFiles sourceFiles}.
	 */
	void setSourceFiles(Object ... paths) {
		_sourceFiles.clear()
		addToListFromVarargs(_sourceFiles, paths)
	}
	
	/**
	 * Adds additional files to {@link #getSourceFiles sourceFiles}
	 */
	void sourceFiles(Object path, Object ... morePaths) {
		addToListFromVarargs1(_sourceFiles, path, morePaths)
	}

	// --- testDir ---
	
	private Object _testDir = { -> sourceDir }
	
	/**
	 * Root directory to look for python unit tests.
	 * <p>
	 * Defaults to {@link #getSourceDir sourceDir }
	 */
	File getTestDir() { project.file(_testDir) }
	
	/**
	 * Set {@link #getTestDir testDir}.
	 */
	void setTestDir(Object path) { _testDir = path }

	// -- useConda ---
	
	/**
	 * Whether to conda-based virtual environment.
	 * <p>
	 * The default is false.
	 * 
	 * @since 0.1.9
	 */
	boolean useConda = false
	
	// --- venv ---
	
	/**
	 * Interface representing virtual environment configured by these settings.
	 */
	PythonVirtualEnvSettings getVenv() { return new PythonVirtualEnvSettings(venvDir) }
	
	// --- venvDir ---
	
	private Object _venvDir =  { new File(this.buildDir, 'venv') }
	
	/**
	 * Location of python virtual environment.
	 * <p>
	 * Defaults to 'venv/' subdirectory of python {@link #buildDir}
	 */
	File getVenvDir() { project.file(_venvDir) }
	
	/**
	 * Set {@link #getVenvDir venvDir}
	 */
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
	
	/**
	 * Sets python {@link #getVersion version}
	 */
	void setVersion(Object version) { _version = version } 

	// --- versionFile ---
	
	private Object _versionFile = null
	
	/**
	 * Location of optional generated version file
	 * <p>
	 * Defaults to null. If specified, configures 'pyversionfile' task
	 * to generate the specified file.
	 */
	File getVersionFile() { 
		_versionFile == null ? null : project.file(_versionFile)
	}
	
	/**
	 * Sets {@link #getVersionFile versionFile}
	 */
	void setVersionFile(Object path) { _versionFile = path }
	
	//--------------
	// Construction
	//

	PythonExtension(Project project) {
		this.project = project
	}

	//---------
	// Methods
	//
	
	/**
	 * Adds artifactory repository package index if not disabled.
	 * <p>
	 * This adds {@link #getArtifactoryUrl artifactoryUrl} to repositories for downloading
	 * unless `noartifactory` project property has been defined.
	 */
	void addArtifactoryRepository() {
		if (!project.hasProperty('noartifactory') || project.getProperty('noartifactory') == null)
			this.repositories(artifactoryUrl)
	}
	
	private String getArtifactoryProperty(String propName)
	{
		propName = artifactoryPrefix + propName
		
		if (project.hasProperty(propName)) {
			def value = project.getProperty(propName)
			if (value != null)
				return stringify(value)
		}
		
		return null
	}
}
