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
import static org.gradle.api.logging.Logging.*;

import java.util.logging.Logger
import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

/**
 * Publish a python distribution to an artifactory repository using HTTP PUT.
 * <p>
 * @author Christopher Barber
 */
class PythonArtifactoryPublishTask extends DefaultTask {
	
	// --- apiKey ---
	
	private Object _apiKey = null
	
	@Internal
	String getApiKey() { stringify(_apiKey) }
	void setApiKey(Object key) { _apiKey = key }
	
	// --- distFiles ---
	
	private List<Object> _distFiles = []
	@InputFiles
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
	@Input getPackageName() { stringify(_packageName) }
	void setPackageName(Object name) { _packageName = name }
	
	// --- password
	
	private Object _password
	@Internal
	String getPassword() { stringify(_password) }
	void setPassword(Object password) { _password = password }

	// --- repositoryKey ---
		
	private Object _repositoryKey
	@Input
	String getRepositoryKey() { stringify(_repositoryKey) }
	void setRepositoryKey(Object key) { _repositoryKey = key }
	
	/// --- repositoryUrl ---
	
	private Object _repositoryUrl
	@Input
	String getRepositoryUrl() { stringify(_repositoryUrl) }
	void setRepositoryUrl(Object url) { _repositoryUrl = url }
	
	/// --- user ---
	
	private Object _user
	String getUser() { stringify(_user) }
	void setUser(Object user) { _user = user }
	
	//------
	// Task
	//
	
	@TaskAction
	void publish() {
		// See https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API
		
		def logger = getLogger();
		def List<String> errors = []
		def useApiKey = apiKey != null
		
		if (!useApiKey) {
			// Setup password authentication
			Authenticator.setDefault(new Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(user, password.toCharArray())
						}
					})
		}

		for (File distFile in distFiles) {
			def url = repositoryUrl + '/' + repositoryKey + '/' + packageName + '/' + distFile.name
			def con = (HttpURLConnection)new URL(url).openConnection()
			
			logger.info(String.format("Upload using %s to %s\n", useApiKey ? "API key" : "password", url));

			// Headers
			con.requestMethod = 'PUT'
			con.setRequestProperty 'UserAgent', 'pygrad/' + project.version
			if (useApiKey) {
				con.setRequestProperty('X-JFrog-Art-Api', apiKey)
			}
			
			// Upload file
			con.doOutput = true
			con.outputStream.withStream { stream -> stream.write distFile.bytes }
			
			// read and parse response
			def responseCode = con.responseCode
			con.inputStream.withStream { stream ->
				def outStr = stream.text
				def  result = new groovy.json.JsonSlurper().parseText(outStr)
				if (result.containsKey('errors')) {
					errors += result.errors[0].message
					print outStr
				}
			}
		}
		
		if (errors) {
			throw new GradleException(errors.iterator.join('\n'))
		}
	}
	
	
}
