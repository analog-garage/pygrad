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
 * Base class for python tasks that run a module as a script using -m
 * <p>
 * @author Christopher Barber
 */
class PythonModuleTaskBase extends PythonExeTaskBase {

	// --- module ---
	
	protected Object _module
	
	@Input
	String getModule() { stringify(_module) }
	void setModule(Object name) { _module = name }
	
	// --- requirement ---
	
	protected Object _requirement
	
	@Input
	String getRequirement() { stringify(_requirement) }
	void setRequirement(Object requirement) { _requirement = requirement }
	
	//--------------
	// Construction
	//
	
	PythonModuleTaskBase(Object module, Object requirement) {
		_module = module
		_requirement = requirement
	}
	
	PythonModuleTaskBase(Object module) {
		this(module, module)
		
		doFirst {
			def env = venv
			if (env != null && env.task != null) {
				env.task.pipRequire(requirement)
			}
		}
	}
}
