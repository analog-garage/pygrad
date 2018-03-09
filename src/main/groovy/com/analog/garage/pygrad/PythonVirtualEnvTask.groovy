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

import org.gradle.StartParameter
import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.external.javadoc.JavadocOfflineLink
import org.gradle.process.ExecResult
import org.gradle.tooling.internal.gradle.BasicGradleProject
import org.gradle.util.*

import com.analog.garage.pygrad.PythonVirtualEnvSettings

import org.gradle.api.logging.*

class PythonVirtualEnvTask extends DefaultTask {

	//------------
	// Properties
	//

	// --- condaEnvFile ---
	
	private Object _condaEnvFile
	
	/**
	 * When using conda, create environment from specified environment file.
	 * <p>
	 * This is only used for the initial environment creation. Any additional
	 * requirements will be added afterwards. If null, no environment file
	 * will be used.
	 */
	@Optional
	@Input
	File getCondaEnvFile() { _condaEnvFile == null ? null : project.file(_condaEnvFile) }
	void setCondaEnvFile(Object path) { _condaEnvFile = path }
	
	// --- condaExe ---
	
	private Object _condaExe = 'conda'
	
	/**
	 * Name or path of conda executable to use for generating environment.
	 * <p>
	 * This only used if {@link #useConda} is true.
	 */
	@Input
	String getCondaExe() { stringify(_condaExe) }
	void setCondaExe(Object exe) { _condaExe = exe }

	// --- condaCreateArgs ---
	
	/**
	 * Arguments for creating initial environment using conda
	 */
	@Internal
	List<String> getCondaCreateArgs() {
		File envFile = condaEnvFile
		
		List<String> args = []
			
		if (envFile != null) {
			if (!envFile.exists()) {
				throw new Error(sprintf('conda environment file %s does not exist', envFile))
			}				
			args += ['env', 'create', '--file', envFile]
		} else {
			args += ['create', '--yes', '--mkdir', '--no-default-packages']
		}

		args += ['-p', venvDir]
				
		args += ['--quiet']
		if (logger.isEnabled(LogLevel.INFO)) {
			args += ['-v']
			if (logger.isEnabled(LogLevel.DEBUG))
				args += ['-v']
		}
		
		if (envFile == null) {
			final boolean offline = project.gradle.startParameter.offline
			
			if (offline)
				args += ['--offline']
				
			args += ['python=' + pythonVersion]
		}
			
		return args
	}
	
	@Internal
	List<String> getCondaInstallArgs() {
		List<String> args = ['install', '-p', venvDir]
		
		args += ['--quiet']
		if (logger.isEnabled(LogLevel.INFO)) {
			args += ['-v']
			if (logger.isEnabled(LogLevel.DEBUG))
				args += ['-v']
		}

		final boolean offline = project.gradle.startParameter.offline
		if (offline)
			args += ['--offline']
			
		return args
	}
	
	// --- pipInstallArgs ---
	
	@Internal
	List<String> getPipInstallArgs() {
		final boolean offline = project.gradle.startParameter.offline
		List<String> args = ['install']
		if (!logger.isEnabled(LogLevel.INFO))
			args += ['-q']
		// In offline mode, use minimal timeout and no retries.
		if (offline)
			args += ['--timeout', '1', '--retries', '0']
		for (String index in repositories) {
			// If offline skip http urls other than localhost
			if (offline && index ==~ '(?i:https?://(?!localhost[:/]).*)')
				continue
			args += ['--extra-index-url', index]
			// Automatically add --trusted-host for http URLs
			def m = index =~ '(?i:http)://([^:/]+).*'
			if (m && m.group(1) != 'localhost') {
				args += ['--trusted-host', m.group(1)]
			}
		}
		return args
	}
	
	// --- pythonExe ---

	private Object _pythonExe = 'python3'
	
	/**
	 * Name or path of python executable to use for generating environment.
	 * <p>
	 * Must refer to an instance of python 3.4 or later (because of dependency
	 * on python's venv package).
	 * <p>
	 * Ignored if {@link #useConda} is true.
	 */
	@Input
	String getPythonExe() { stringify(_pythonExe) }
	void setPythonExe(Object exe) { _pythonExe = exe }

	// -- pythonVersion ---
	
	private Object _pythonVersion = '3.6'
	
	/**
	 * Version of python to use in environment when using conda.
	 * <p>
	 * This is ignored if {@link #useConda} is false (the default).
	 */
	@Input
	String getPythonVersion() { stringify(_pythonVersion) }
	void setPythonVersion(Object version) { _pythonVersion = version }
	
	// -- repositories --

	private List<Object> _repositories = []
	
	@Input
	List<String> getRepositories() { stringifyList(_repositories) }
	void setRepositories(Object ... additional) {
		_repositories.clear()
		addToListFromVarargs(_repositories, additional)
	}
	void repositories(Object repo, Object ... additional) {
		addToListFromVarargs1(_repositories, repo, additional)
	}

	// -- requirements --

	private List<String> _requirements = []
	@Input
	Set<String> getRequirements() { stringifySet(_requirements) }
	void setRequirements(Object ... requirements) {
		_requirements.clear()
		addToListFromVarargs(_requirements, requirements)
	}
	void requirements(Object requirement, Object ... more) {
		addToListFromVarargs1(_requirements, requirement, more)
	}
	void require(Object requirement) {
		_requirements.add(requirement)
	}

	// --- upgrades ---

	private List<Object> _upgrades = ['pip', 'setuptools']

	@Input
	List<String> getUpgrades() { stringifyList(_upgrades) }
	void setUpgrades(Object ... upgrades) {
		_upgrades.clear()
		addToListFromVarargs(_upgrades, upgrades)
	}
	void upgrades(Object upgrade, Object ... upgrades) {
		addToListFromVarargs1(_upgrades, upgrade, upgrades)
	}

	// --- venv ---
	
	@Internal
	PythonVirtualEnvSettings getVenv() { new PythonVirtualEnvSettings(this) }
	
	// --- venvDir --- 

	private Object _venvDir = "${project.buildDir}/python/venv"

	@OutputDirectory
	File getVenvDir() { project.file(_venvDir) }

	void setVenvDir(Object dir) { _venvDir = dir }
	
	// --- sourceDirs ---
	
	private final List<Object> _sourceDirs = []
	
	@Input
	FileCollection getSourceDirs() { project.files(_sourceDirs) }
	void setSourceDirs(Object ... dirs) { 
		_sourceDirs.clear()
		addToListFromVarargs(_sourceDirs, dirs)
	}
	void sourceDirs(Object dir, Object ... dirs) {
		addToListFromVarargs1(_sourceDirs, dir, dirs)
	}
	
	// --- useConda ---
	
	/**
	 * Specifies whether to use conda-based virtual environment.
	 * <p>
	 * If true, then the virtual environment will be created using
	 * conda instead of the Python 3 venv module.
	 * <p>
	 * The default is false.
	 */
	@Input
	boolean useConda = false
	
	// --- useSymlinks ---
	
	/**
	 * Whether to use symlinks to system executables.
	 * <p>
	 * The default is false, which is currently needed if you want
	 * PyCharm to correctly find your virtual env directory due to
	 * a bug (https://youtrack.jetbrains.com/issue/PY-21787).
	 */
	@Input
	boolean useSymlinks = false

	// --- useSystemSitePackages ---
	
	@Input
	boolean useSystemSitePackages = true
	
	//--------------
	// Construction
	//

	PythonVirtualEnvTask() {
		description = 'Create python virtual environment, if missing'
	}

    //------
    // Task
    //

	@TaskAction
	void createVirtualEnv() {
		if (useConda) {
			condaCreate()
			return
		}
			
		project.exec {
			executable = pythonExe
			args = ['-c', '''
import sys
if sys.hexversion < 0x030400F0:
    print('pythonExe must be at least Python 3.4')
    sys.exit(42)		
''']
		}
		
		def venvArgs = ['-m', 'venv']
		if (useSymlinks)
			venvArgs += '--symlinks'
		else
			venvArgs += '--copies'
		venvArgs += venvDir
		
		project.exec {
			executable = pythonExe
			args = venvArgs
		}
		
		if (useSystemSitePackages) {
			// Run again with site packages flag. We cannot use this
			// the first time because then it won't install pip
			// see https://bugs.python.org/issue24875
			venvArgs += '--system-site-packages'
			project.exec {
				executable = pythonExe
				args = venvArgs
			}
		}

		def venv = new PythonVirtualEnvSettings(venvDir)

		for (pkg in upgrades) {
			pipUpgrade(pkg)
		}

		for (requirement in requirements) {
			pipRequire(requirement)
		}
	}
	
	//---------
	// Methods
	//
	
	/**
	 * Install a package with given requirements specification
	 * <p>
	 * Uses conda if {@link #useConda} is true otherwise uses pip.
	 * <p>
	 * @param requirement is a pip/conda package requirement string
	 */
	void install(String requirement) {
		if (useConda) {
			condaInstall(requirement)
		} else {
			pipRequire(requirement)
		}
	}
	
	/**
	 * Creates environment using conda.
	 */
	void condaCreate() {
		// Create basic environment
		project.exec {
			executable = condaExe
			args = condaCreateArgs
		}
		
		// Then add requirements
		for (requirement in requirements) {
			condaInstall(requirement)
		}
	}
	
	void condaInstall(String requirement) {
		ExecResult result = project.exec {
			executable = condaExe
			args = condaInstallArgs + [requirement]
			ignoreExitValue = true
		}
		
		if (result.exitValue != 0) {
			// If conda doesn't work, try pip.
			// TODO - look at actual error message
			pipRequire(requirement)
		}
	}
	
	void pipUpgrade(String pkg) {
		def installArgs = pipInstallArgs + ['--upgrade', pkg]
		project.exec {
			executable = venv.pipExe
			args = installArgs
		}
	}
	
	void pipRequire(String requirement) {
		project.exec {
			executable = venv.pipExe
			args = pipInstallArgs + [requirement]
		}
	}
}
