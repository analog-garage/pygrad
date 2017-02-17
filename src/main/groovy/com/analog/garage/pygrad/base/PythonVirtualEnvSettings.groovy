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

class PythonVirtualEnvSettings {
	
	private final boolean isWindows
	
	final public File rootDir
	final public File binDir

	File getPythonExe() { exe('python') }
	File getPipExe() { exe('pip') }
	File getCoverageExe() { exe('coverage') }
	
	/**
	 * Location of sphinx-build script in environment.
	 */
	File getSphinxBuildExe() { exe('sphinx-build') }

	PythonVirtualEnvSettings(File rootDir) {
		this.rootDir = rootDir
		isWindows = System.properties['os.name'].toLowerCase().contains('windows')
		binDir = new File(rootDir, isWindows ? "Scripts" : "bin")
	}

	File exe(String name) {
		new File(binDir, isWindows? name + '.exe' : name)
	}
}