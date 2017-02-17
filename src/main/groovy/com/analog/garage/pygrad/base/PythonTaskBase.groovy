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

import org.gradle.api.DefaultTask
import org.gradle.internal.impldep.aQute.bnd.version.MavenVersion

import com.analog.garage.pygrad.base.PythonVirtualEnvSettings

/**
 * Base class for python tasks that need to run executable from virtual environment
 * @author Christopher Barber
 */
abstract class PythonTaskBase extends DefaultTask {

	private Object _venv = null
	
	PythonVirtualEnvSettings getVenv() { _venv = resolveCallable(_venv) }
	void setVenv(Object env) { _venv = env }
}
