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

/**
 * @author Christopher Barber
 */
class DevpiTaskBase extends PythonModuleTaskBase {

	// --- devpiExe ---
	
	private Object _devpiExe = null
	
	// --- devpiIndex ---
	
	private Object _devpiIndex = null
	@Input
	String getDevpiIndex() { stringifyWithDefaults(_devpiIndex, project.ext, 'devpiIndex', 'dev') }
	void setDevpiIndex(Object index) { _devpiIndex = index }
	
	// --- devpiPassword ---
	
	private Object _devpiPassword = null
	@Internal
	String getDevpiPassword() { stringifyWithDefaults(_devpiPassword, project.ext, 'devpiPassword', 'password') }
	void setDevpiPassword(Object pwd) { _devpiPassword = pwd }
	
	// --- devpiPort ---
	
	private Object _devpiPort = null
	@Input
	String getDevpiPort() { stringifyWithDefaults(_devpiPort, project.ext, 'devpiPort', '3141') }
	void setDevpiPort(Object port) { _devpiPort = port }
	
	// --- devpiUser ---
	
	private Object _devpiUser = null
	@Input
	String getDevpiUser() { stringifyWithDefaults(_devpiUser, project.ext, 'devpiUser', 'user') }
	void setDevpiUser(Object user) { _devpiUser = user }
	
	// --- devpiUrl ---
	
	@Internal
	String getDevpiUrl() {
		'http://localhost:' + devpiPort + '/' + devpiUser + '/' + devpiIndex
	}

	//--------------
	// Construction
	//
	
	DevpiTaskBase() {
		super("devpi")
	}
	
	//------
	// Task	
	//
	
	void devpi(String ... devpiArgs) {
		def execArgs = ['-m', module] + (devpiArgs as List)
		project.exec {
			executable = pythonExe
			args = execArgs
		}
	}
	
	void login() {
		devpi('login', devpiUser, '--password', devpiPassword)
	}
	
	void logoff() {
		devpi('logoff')
	}
	
	void use(String url) {
		devpi('use', url != null ? url : devpiUrl)
	}
}
