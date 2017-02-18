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
import org.gradle.api.tasks.*

import groovy.json.StringEscapeUtils

/**
 * Generates a Python file containing variable definitions.
 * <p>
 * @author Christopher Barber
 */
class PythonVariableFileTask extends DefaultTask
{
	//------------
	// Properties	
	//

	// --- header ---
	
	private Object _header = 'This file is generated.'
	
	@Input
	String getHeader() { stringify(_header) }
	void setHeader(Object header) { _header = header }
	
	// --- outputFile ---
	
	private Object _outputFile
	
	@OutputFile
	File getOutputFile() { return project.file(_outputFile) }
	void setOutputFile(Object file) { _outputFile = file }
	
	private Map<String, Object> _variables = [:]
	@Input
	Map<String, Object> getVariables() {
		return _variables
	}
	void setVariables(Map<String, Object> vars) {
		_variables.clear()
		if (vars != null)
			_variables.putAll(vars);
	}
	void variables(Map<String,Object> vars) {
		if (vars != null)
			_variables.putAll(vars)
	}
	void variable(String key, Object value) {
		_variables.put(key, value)
	}
	
	//------
	// Task
	//
	
	@TaskAction
	void generate()
	{
		outputFile.withWriter { out ->
			if (header != null) {
				for (line in header.split('"\\r?\\n')) {
					if (!line.startsWith('#'))
						out.write('# ')
					out.write(line)
					out.newLine()
				}
			}
			for (var in variables) {
				out.write(var.key)
				out.write(' = ')
				out.write(toPython(var.value))
				out.newLine()
			}
		}
	}
	
	String toPython(Object value)
	{
		if (value instanceof Number)
			return value.toString()
			
		if (value instanceof CharSequence)
			return "'" + StringEscapeUtils.escapeJavaScript(value.toString()) + "'"
			
		if (value instanceof Boolean)
			return (boolean)value ? 'True' : 'False'
		
		if (value instanceof List) {
			def sb = new StringBuilder('[ ')
			boolean writeComma = false
			for (elt in (value as List)) {
				if (writeComma)
					sb.append(', ')
				sb.append(toPython(elt))
				writeComma = true
			}
			sb.append(' ]')
			return sb.toString()
		}
		
		if (value instanceof Set) {
			def sb = new StringBuilder('{ ')
			boolean writeComma = false
			for (elt in (value as List)) {
				if (writeComma)
					sb.append(', ')
				sb.append(toPython(elt))
				writeComma = true
			}
			sb.append(' }')
			return sb.toString()
		}

		throw new GradleException(sprintf("Cannot convert '%s' to python", value))
	}
}
