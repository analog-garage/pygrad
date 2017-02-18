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

/**
 * Publish a python distribution to an artifactory repository.
 * <p>
 * @author Christopher Barber
 */
class PythonArtifactoryPublishTask extends DefaultTask {
	
	// --- distFiles ---
	
	private List<Object> _distFiles = []
	@InputFile
	FileCollection getDistFiles() { project.files(_distFiles) }
	void setDistFiles(Object ... files) {
		_distFiles.clear()
		addToListFromVarargs(_distFiles, files)
	}
	void distFiles(Object file, Object ... files) {
		addToListFromVarargs1(_distFiles, file, files)
	}
	
	// --- packageName ---
	
	private Object _packageName
	@Input getPackageName() { stringize(_packageName) }
	void setPackageName(Object name) { _packageName = name }
	
	// --- password
	
	private Object _password
	String getPassword() { stringify(_password) }
	void setPassword(Object password) { _password = password }

	// --- repositoryKey ---
		
	private Object _repositoryKey
	@Input
	String getRepositoryKey() { stringify(_repositoryLey) }
	void setRepositoryKey(Object key) { _repositoryKey = key }
	
	/// --- repositoryUrl ---
	
	private Object _repositoryUrl
	@Input
	String getRepositoryUrl() { stringify(_repositoryUrl) }
	void setRepositoryUrl(Object url) { _repositoryUrl = url }
	
	/// --- user ---
	
	private Object _user
	@Input
	String getUser() { stringify(_user) }
	void setUser(Object user) { _user = user }
	
	//------
	// Task
	//
	
	@TaskAction
	void publish() {
		// See https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API
		
		def List<String> errors = []
		
		for (distFile in distFiles) {
			def out = new ByteArrayOutputStream()
			project.exec {
				executable = curlExe
				args = [ '-u', user + ':' + password, '-X', 'PUT',
					repositoryUrl + '/' + repositoryKey + '/' + packageName + '/' + distFile,
					'-T', distFile
				]
			}
			def outStr = standardOutput.toString()
			def result = new groovy.json.JsonSlurper().parseText(outStr)
			if (result.containsKey('errors')) {
				errors += result.errors[0].message
				print outStr
			}
		}
		
		if (errors) {
			throw new GradleException(errors.iterator.join('\n'))
		}
	}
}
