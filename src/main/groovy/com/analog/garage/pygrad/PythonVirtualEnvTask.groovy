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

/**
 * Task creates Python virtual environment in build subdirectory.
 * <p>
 * This will create a conda environment if {@link #useConda} is true, and
 * otherwise will use the Python venv module and pip.
 * <p>
 * @author Christopher Barber
 */
class PythonVirtualEnvTask extends DefaultTask {

	//------------
	// Properties
	//

	// --- condaChannels ---
	
	private List<Object> _condaChannels = []
	
	/**
	 * Additional conda channels to search.
	 * <p>
	 * A popular choice is {@code 'conda-forge'}. The name 'nodefaults'
	 * will cause default channels from being considered.
	 * <p>
	 * @since 0.1.10
	 */
	@Input
	List<String> getCondaChannels() { stringifyList(_condaChannels) }
	void setCondaChannels(Object ... additional) {
		_condaChannels.clear()
		addToListFromVarargs(_condaChannels, additional)
	}
	void condaChannels(Object channel, Object ... additional) {
		addToListFromVarargs1(_condaChannels, channel, additional)
	}
	
	// --- condaEnvFile ---
	
	private Object _condaEnvFile
	
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
	@Optional
	@Input
	File getCondaEnvFile() { _condaEnvFile == null ? null : project.file(_condaEnvFile) }
	void setCondaEnvFile(Object path) { _condaEnvFile = path }
	
	// --- condaExe ---
	
	private Object _condaExe = 'conda'
	
	/**
	 * Name or path of conda executable to use for generating environment.
	 * <p>
	 * Default is 'conda'
	 * <p>
	 * This only used if {@link #useConda} is true.
	 * 
	 * @since 0.1.9
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
	
	/**
	 * Arguments for installing a package using conda.
	 */
	@Internal
	List<String> getCondaInstallArgs() {
		List<String> args = ['install', '-p', venvDir]
		
		args += ['--quiet']
		if (logger.isEnabled(LogLevel.INFO)) {
			args += ['-v']
			if (logger.isEnabled(LogLevel.DEBUG))
				args += ['-v']
		}

		for (String channel in condaChannels) {
			if (channel == 'nodefaults') {
				args += ['--override-channels']
			} else {
				args += ['--channel', channel]
			}
		}
		
		final boolean offline = project.gradle.startParameter.offline
		if (offline)
			args += ['--offline']
			
		return args
	}
	
	@Internal
	List<String> getCondaUpdateArgs() {
		def args = condaInstallArgs
		args[0] = 'update'
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
	 * This may refer 2.7, though it is not guaranteed that all tasks will
	 * still work (e.g. there is no coverage module in 2.7, so the coverage task
	 * will not work out of the box under 2.7)
	 * <p>
	 * This is ignored if {@link #useConda} is false (the default).
	 * <p>
	 * @since 0.1.9
	 */
	@Input
	String getPythonVersion() { stringify(_pythonVersion) }
	void setPythonVersion(Object version) { _pythonVersion = version }
	
	// -- repositories --

	private List<Object> _repositories = []
	
	/**
	 * Additional pypi repositories to search.
	 */
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
	
	/**
	 * List of pypi requirements specifiers for packages to install.
	 */
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

	private List<Object> _upgrades = []

	/**
	 * List of packages that should be upgraded after installation.
	 * <p>
	 * 'pip' and 'setuptools' will automatically be upgraded if not using conda
	 */
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
	
	/**
	 * Attributes of the python virtual environment.
	 */
	@Internal
	PythonVirtualEnvSettings getVenv() { new PythonVirtualEnvSettings(this) }
	
	// --- venvDir --- 

	private Object _venvDir = "${project.buildDir}/python/venv"

	/**
	 * The location of the python virtual environment for this project.
	 * <p>
	 * By default this is in the {@code python/venv} subdirectory of the build directory.
	 */
	@OutputDirectory
	File getVenvDir() { project.file(_venvDir) }

	void setVenvDir(Object dir) { _venvDir = dir }
	
	// --- sourceDirs ---
	
	private final List<Object> _sourceDirs = []
	
	/**
	 * List of root source directories for python content.
	 */
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
	 * conda instead of the Python 3 {@code venv} module.
	 * <p>
	 * The default is false.
	 * 
	 * @since 0.1.9
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
	 * <p>
	 * This does not apply to the 
	 */
	@Input
	boolean useSymlinks = false

	// --- useSystemSitePackages ---
	
	/**
	 * Specifies whether pip based virtual environment should link to system site-packages.
	 * <p>
	 * This can save a significant amount of space.
	 */
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

		pipUpgrade('pip')
		pipUpgrade('setuptools')
		
		for (requirement in requirements) {
			pipRequire(requirement)
		}

		for (pkg in upgrades) {
			pipUpgrade(pkg)
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
	 * @since 0.1.9
	 */
	void install(String requirement) {
		if (useConda) {
			condaInstall(requirement)
		} else {
			pipRequire(requirement)
		}
	}
	
	/**
	 * Update given requirement to latest version.
	 * <p>
	 * Uses conda if {@link #useConda} is true otherwise uses pip.
	 * <p>
	 * @param requirement is a pip/conda package requirement string
	 * @since 0.1.10
	 */
	void update(String requirement) {
		if (useConda) {
			condaUpdate(requirement)
		} else {
			pipUpgrade(requirement)
		}
	}
	
	/**
	 * Creates environment using conda.
	 * @since 0.1.9
	 */
	void condaCreate() {
		// Create basic environment
		if (venvDir.exists()) {
			condaInstall('python='+pythonVersion)
		} else {
			project.exec {
				executable = condaExe
				args = condaCreateArgs
			}
		}
				
		// Then add requirements
		for (requirement in requirements) {
			condaInstall(requirement)
		}
	}
	
	/**
	 * Install package using conda
	 * @since 0.1.9
	 */
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
	
	/**
	 * Update package using conda
	 * @since 0.1.10
	 */
	void condaUpdate(String requirement) {
		ExecResult result = project.exec {
			executable = condaExe
			args = condaUpdateArgs + [requirement]
			ignoreExitValue = true
		}
		
		if (result.exitValue != 0) {
			// If conda doesn't work, try pip.
			// TODO - look at actual error message
			pipUpgrade(requirement)
		}
	}
	
	void pipUpgrade(String pkg) {
		// If we are upgrading pip, do it this way...makes it work for Python3 on Windows 10
		// (and probably all other platforms)
		if (pkg == 'pip'){
			def installArgs = ['-m', 'pip', 'install', '--upgrade', 'pip']
			project.exec {
				executable = venv.pythonExe
				args = installArgs
			}
		} else {
			def installArgs = pipInstallArgs + ['--upgrade', pkg]
			project.exec {
				executable = venv.pipExe
				args = installArgs
			}
		}
	}
	
	void pipRequire(String requirement) {
		project.exec {
			executable = venv.pipExe
			args = pipInstallArgs + [requirement]
		}
	}
}
