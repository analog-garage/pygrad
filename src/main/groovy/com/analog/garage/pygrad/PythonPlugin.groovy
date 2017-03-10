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

import org.gradle.api.*
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.plugins.BasePlugin

/**
 * The standard pygrad plugin.
 * <p>
 * This adds a {@link PythonExtension} to the project under the name
 * 'python' that can be used to globally configure python task defaults.
 * <p>
 * It adds the following tasks:
 * <ul>
 * <li>pyenv - creates and configures python virtual environment for project
 * <li>pyversionfile - generates optional package version file
 * <li>pysources - lifecyle task for generation of python sources
 * <li>pydevelop - installs link to project source to virtual environment
 * <li>pytest - runs python unit tests
 * <li>pycoverage - runs python unit tests while measuring code coverage
 * <li>pycoverageReport - outputs report of python code coverage
 * <li>pycoverageHTML - generates HTML reports of python code coverage
 * <li>pydoc - generates python API documentation
 * <li>pydist - generates python package distribution file(s)
 * <li>pyuploadLocal - uploads python package to local devpi repository
 * </ul>
 * <p>
 * @author Christopher Barber
 */
class PythonPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.getPluginManager().apply(BasePlugin.class)
		
		def pyext = project.extensions.create('python', PythonExtension, project)

		// Tasks with a group property will show up in 'gradle tasks'.
		// Other tasks will only be seen using 'gradle tasks --all'.
		
		def venvTask = project.task(
			type: PythonVirtualEnvTask, 
			group: 'Python', 
			'pyenv')
		def sourcesTask = project.task(
			group: 'Python', 
			description: 'Builds any generated python sources',
			'pysources')
		def testTask = project.task(
			type: PythonUnitTestTask,
			group: 'Python', 
			'pytest')
		def coverageTask = project.task(
			type: PythonCoverageTask,
			group: 'Python',
			'pycoverage')
		def reportCoverageTask = project.task(
			type: PythonCoverageReportTask,
			group: 'Python',
			'pycoverageReport')
		def htmlCoverageTask = project.task(
			type: PythonCoverageHTMLTask,
			group: 'Python',
			'pycoverageHTML')
		def docTask = project.task(
			type: SphinxDocTask,
			group: 'Python',
			description: 'Generates python documentation',
			'pydoc')
		def distTask = project.task(
			type: PythonDistTask,
			group: 'Python',
			description: 'Builds python package distribution',
			'pydist')
		
		def developTask = project.task(
			type: PythonSetupTask,
			group: 'Python',
			description: 'Installs link to source in virtual env',
			'pydevelop'
			)
		def cleanDevelopTask = project.task(
			type: PythonSetupTask,
			description: 'Uninstalls link to source from virtual env',
			'cleanPydevelop'
			)
			
		def devpiLoginTask = project.task(
			type: DevpiTaskBase,
			description: 'Login to devpi on localhost using credentials from gradle.properties',
			action: { login() },
			'devpiLogin'
			)
		
		def devpiUploadTask = project.task(
			type: DevpiUploadTask,
			group: 'Python',
			description: 'Upload python distribution to devpi server on localhost',
			'pyuploadLocal')
		
		def versionFileTask = project.task(
			type: PythonVariableFileTask,
			group: 'Python',
			description: 'Creates optional version file if specified',
			'pyversionfile')
		
		def artifactoryPublishTask = project.task(
			type: PythonArtifactoryPublishTask,
			group: 'Python',
			description: 'Publishes python package to artifactory repository',
			'artifactoryPublishPython')
		
		project.afterEvaluate {
			venvTask.configure
			{
				pythonExe = pyext.pythonExe
				venvDir = pyext.venvDir
				requirements pyext.buildRequirements
				requirements pyext.requirements
				repositories pyext.repositories
				sourceDirs pyext.sourceDir
			}
			
			testTask.configure {
				dependsOn sourcesTask, developTask
				testDir = pyext.testDir
				sourceFiles = pyext.sourceFiles
			}
			coverageTask.configure {
				dependsOn sourcesTask, developTask
				testDir = pyext.testDir
				sourceFiles = pyext.sourceFiles
				coverageDir = pyext.coverageDir
				coverageFile = pyext.coverageFile
				
			}
			reportCoverageTask.configure {
				coverageDir = pyext.coverageDir
				coverageFile = pyext.coverageFile
				dependsOn coverageTask
			}
			htmlCoverageTask.configure {
				coverageDir = pyext.coverageDir
				coverageFile = pyext.coverageFile
				outputDir = pyext.coverageHtmlDir
				dependsOn coverageTask
			}
			docTask.configure {
				sourceDir = pyext.docSourceDir
				outputDir = pyext.docsDir
				inputs.files pyext.sourceDir
				dependsOn sourcesTask, developTask
			}
			distTask.configure {
				outputDir = pyext.distDir
				setupFile = pyext.setupFile
				dependsOn sourcesTask
			}
			developTask.configure {
				setupFile = pyext.setupFile
				setupArgs = ['develop']
				dependsOn sourcesTask
			}
			cleanDevelopTask.configure {
				setupFile = pyext.setupFile
				setupArgs = ['develop', '--uninstall']
			}
			
			devpiUploadTask.configure {
				dependsOn distTask
				distDir = distTask.outputDir
				setupFile = pyext.setupFile
			}
			
			artifactoryPublishTask.configure {
				dependsOn distTask
				user = pyext.artifactoryUser
				password = pyext.artifactoryPassword
				repositoryUrl = pyext.artifactoryBaseUrl
				repositoryKey = pyext.artifactoryKey
				distFiles = { pydist.OutputFiles }
				packageName = pyext.packageName
			}
			
			versionFileTask.configure {
				header = 'This file was generated by gradle. Do not check in.'
				variables __version__: { pyext.version }, __requires__: { pyext.requirements }
				outputFile = { pyext.versionFile }
			}
			sourcesTask.dependsOn versionFileTask
			
			for (t in project.tasks) {
				// Make all tasks using virtual env depend on the default virtual
				// environment by default.
				if (t instanceof PythonTaskBase) {
					t.dependsOn(venvTask)
					t.venv = { (venvTask as PythonVirtualEnvTask).venv }
				}
				
				if (t instanceof DevpiTaskBase) {
					t.devpiUser = pyext.devpiUser
					t.devpiPassword = pyext.devpiPassword
					t.devpiPort = pyext.devpiPort
					t.devpiIndex = pyext.devpiIndex
				}
			}
		}
	}
}

