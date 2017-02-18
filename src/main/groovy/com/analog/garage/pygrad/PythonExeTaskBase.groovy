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

import org.gradle.api.tasks.Input

import com.analog.garage.pygrad.PythonTaskBase

/**
 * Base class for python tasks that use the python executable.
 * <p>
 * @author Christopher Barber
 */
class PythonExeTaskBase extends PythonTaskBase {

	private Object _pythonExe = null
	
	@Input
	String getPythonExe() {
		if (_pythonExe == null) {
			return venv != null ? venv.pythonExe : 'python'
		}
		return stringify(_pythonExe) 
	}
	void setPythonExe(Object exe) { _pythonExe = exe }

}
