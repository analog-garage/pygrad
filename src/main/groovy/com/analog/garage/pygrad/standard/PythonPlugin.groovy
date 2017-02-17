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

package com.analog.garage.pygrad.standard

import org.gradle.api.*

import com.analog.garage.pygrad.base.*

class PythonPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.extensions.create('python', PythonExtension, project)

		def venvTask = project.task(
			type: PythonVirtualEnvTask, 
			group: 'Build', 
			'pyenv')
		def buildTask = project.task(
			group: 'Build', 
			'pybuild')
		def testTask = project.task(
			type: PythonUnitTestTask,
			group: 'Verification', 
			'pytest')
		def coverageTask = project.task(
			type: PythonCoverageTask,
			group: 'Verification',
			'pycoverage')
		def reportCoverageTask = project.task(
			type: PythonCoverageReportTask,
			group: 'Verification',
			'pycoverageReport')
		def htmlCoverageTask = project.task(
			type: PythonCoverageHTMLTask,
			group: 'Verification',
			'pycoverageHTML')
		def docTask = project.task(
			type: SphinxDocTask,
			group: 'Documentation',
			description: 'Generates python documentation',
			'pydoc')
		def distTask = project.task(
			type: PythonDistTask,
			group: 'Build',
			description: 'Builds python package distribution',
			'pydist')
		
		def developTask = project.task(
			type: PythonSetupTask,
			group: 'Build',
			description: 'Installs link to source in virtual env',
			'pydevelop'
			)
		def cleanDevelopTask = project.task(
			type: PythonSetupTask,
			group: 'Clean',
			description: 'Uninstalls link to source from virtual env',
			'cleanPydevelop'
			)
			
		project.afterEvaluate {
			venvTask.configure
			{
				pythonExe = project.python.pythonExe
				venvDir = project.python.venvDir
				requirements project.python.buildRequirements
				requirements project.python.requirements
				repositories project.python.repositories
				sourceDirs project.python.sourceDir
			}
			
			testTask.configure {
				dependsOn buildTask
				testDir = "$project.python.sourceDir"
			}
			coverageTask.configure {
				dependsOn buildTask
				testDir = "$project.python.sourceDir"
				coverageDir = "$project.python.coverageDir"
				coverageFile = "$project.python.coverageFile"
				
			}
			reportCoverageTask.configure {
				coverageDir = "$project.python.coverageDir"
				coverageFile ="$project.python.coverageFile"
				dependsOn coverageTask
			}
			htmlCoverageTask.configure {
				coverageDir = "$project.python.coverageDir"
				coverageFile ="$project.python.coverageFile"
				outputDir = "$project.python.coverageHtmlDir"
				dependsOn coverageTask
			}
			docTask.configure {
				sourceDir = "$project.python.docSrcDir"
				inputs.files("$project.python.sourceDir")
				dependsOn buildTask
			}
			distTask.configure {
				outputDir = "$project.python.buildDir/distributions"
				setupFile = "$project.python.setupFile"
			}
			developTask.configure {
				setupFile = "$project.python.setupFile"
				setupArgs = ['develop']
			}
			cleanDevelopTask.configure {
				setupFile = "$project.python.setupFile"
				setupArgs = ['develop', '--uninstall']
			}
			publishTask.configure {
				packageName = "$project.python.packageName"
				distFiles = { distTask.outputFiles }
			}
			
			// Make all tasks using virtual env depend on the default virtual
			// environment by default.
			project.tasks.forEach {
				if (it instanceof PythonTaskBase) {
					def pytask = it
					it.dependsOn(venvTask)
					it.venv = { (venvTask as PythonVirtualEnvTask).venv }
				}
			}
		}
	}
}

