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

import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.util.*

import com.analog.garage.pygrad.PythonVirtualEnvSettings

import org.gradle.api.logging.*

class PythonVirtualEnvTask extends DefaultTask {

	//------------
	// Properties
	//

	// --- pipInstallArgs ---
	
	@Internal
	List<String> getPipInstallArgs() {
		List<String> args = ['install']
		if (!logger.isEnabled(LogLevel.INFO))
			args += ['-q']
		for (String index in repositories) {
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
	 */
	@Input
	String getPythonExe() { stringify(_pythonExe) }
	void setPythonExe(Object exe) { _pythonExe = exe }

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
		def log = Logging.getLogger(getClass())
		
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
