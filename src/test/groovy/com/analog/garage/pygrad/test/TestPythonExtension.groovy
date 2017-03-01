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

package com.analog.garage.pygrad.test

import static PygradTestUtils.*

import com.analog.garage.pygrad.DevpiTaskBase
import com.analog.garage.pygrad.PythonExtension
import com.analog.garage.pygrad.PythonVariableFileTask

import java.security.GeneralSecurityException

import org.gradle.initialization.AbstractProjectSpec
import org.gradle.internal.impldep.aQute.bnd.version.MavenVersion
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.internal.gradle.PartialBasicGradleProject
import org.junit.Test

/**
 * @author Christopher Barber
 */
class TestPythonExtension extends PygradTestBase {

	@Test
	void test() {
		
		def project = testProjectBuilder('simple', folder).build()
		
		project.version = '1.2'
			
		def python = new PythonExtension(project)
		assert python.project == project
		
		// buildDir
		assert python.buildDir == new File(project.buildDir, 'python')
		python.buildDir = "$project.buildDir/barf"
		assert python.buildDir == new File(project.buildDir, 'barf')
		python._buildDir = "$project.buildDir/python"
		
		// buildRequirements
		assert [] as Set == python.buildRequirements
		python.buildRequirements 'one', 'two'
		assert ['one', 'two'] as Set == python.buildRequirements
		python.buildRequirements 3
		assert ['one', 'two', '3'] as Set == python.buildRequirements
		python.buildRequirements = []
		assert [] as Set == python.buildRequirements
		
		// coverageDir
		assert new File(python.buildDir, 'coverage') == python.coverageDir
		python.buildDir = "$project.buildDir/py"
		assert new File(project.buildDir, 'py/coverage') == python.coverageDir
		python.coverageDir = { 'pycoverage' }
		assert new File(project.rootDir, 'pycoverage') == python.coverageDir
		python.buildDir = "$project.buildDir/python"
		python.coverageDir = "$project.buildDir/python/coverage"
		
		// coverageFile
		assert new File(python.coverageDir, 'python.coverage') == python.coverageFile
		python.coverageFile = 'foo.coverage'
		assert new File(python.coverageDir, 'foo.coverage') == python.coverageFile
		python.coverageFile = { new File('barf') }
		assert new File('barf') == python.coverageFile
		python.coverageFile = 'python.coverage'
		
		// coverageHtmlDir
		assert new File(python.coverageDir, 'html') == python.coverageHtmlDir
		python.coverageHtmlDir = 'foo'
		assert new File(python.coverageDir, 'foo') == python.coverageHtmlDir
		python.coverageHtmlDir = { new File('barf') }
		assert new File('barf') == python.coverageHtmlDir
		python.coverageHtmlDir = 'html'
		
		// devpiIndex
		assert 'dev' == python.devpiIndex
		assert 'http://localhost:3141/user/dev' == python.devpiUrl
		project.ext.setProperty('devpiIndex', 'index')
		assert 'index' == python.devpiIndex
		assert 'http://localhost:3141/user/index' == python.devpiUrl
		python.devpiIndex = { 'barf' }
		assert 'barf' == python.devpiIndex
		project.ext.setProperty('devpiIndex', null)
		python.devpiIndex = null
		assert 'dev' == python.devpiIndex
		
		// devpiPassword
		assert 'password' == python.devpiPassword
		project.ext.setProperty('devpiPassword', '???')
		assert '???' == python.devpiPassword
		python.devpiPassword = { 'barf' }
		assert 'barf' == python.devpiPassword
		project.ext.setProperty('devpiPassword', null)
		python.devpiPassword = null
		assert 'password' == python.devpiPassword
		
		// devpiPort
		assert '3141' == python.devpiPort
		project.ext.setProperty('devpiPort', '8083')
		assert '8083' == python.devpiPort
		assert 'http://localhost:8083/user/dev' == python.devpiUrl
		python.devpiPort = { '4123' }
		assert '4123' == python.devpiPort
		project.ext.setProperty('devpiPort', null)
		python.devpiPort = null
		assert '3141' == python.devpiPort

		// devpiUser
		assert 'user' == python.devpiUser
		project.ext.setProperty('devpiUser', 'bob')
		assert 'bob' == python.devpiUser
		assert 'http://localhost:3141/bob/dev' == python.devpiUrl
		python.devpiUser = { 'mary' }
		assert 'mary' == python.devpiUser
		project.ext.setProperty('devpiUser', null)
		python.devpiUser = null
		assert 'user' == python.devpiUser
		
		// distDir
		assert new File(python.buildDir, 'dist') == python.distDir
		python.distDir = 'distributions'
		assert new File(project.rootDir, 'distributions') == python.distDir
		python.distDir = null
		
		// docsDir
		assert new File(python.buildDir, 'docs') == python.docsDir
		python.docsDir = "$project.buildDir/docs/python"
		assert new File(project.buildDir, "docs/python") == python.docsDir
		python._docsDir = 'docs'
		
		// docSourceDir
		assert new File(project.rootDir, 'doc/python-api') == python.docSourceDir
		python.docSourceDir = 'doc/python-api'
		
		// packageName
		assert 'simple' == python.packageName
		python.packageName = 'pyproj'
		assert 'pyproj' == python.packageName
		python.packageName = { 'foo' }
		assert 'foo' == python.packageName
		python._packageName = "$project.name"
		assert 'simple' == python.packageName
		
		// pythonExe
		assert 'python3' == python.pythonExe
		python.pythonExe = { 'python3.5' }
		assert 'python3.5' == python.pythonExe
		
		// repositories
		assert [] == python.repositories
		python.repositories 'one', { 2 }
		python.repositories "3"
		assert ['one', '2', '3'] == python.repositories
		python.repositories = []
		assert [] == python.repositories
		
		// requirements
		assert [] as Set == python.requirements
		python.require 'coverage'
		python.requirements ({ "antlr>=4.6" }, 'barf==3.4')
		assert ['coverage', 'antlr>=4.6', 'barf==3.4'] as Set == python.requirements
		python.requirements = []
		assert [] as Set == python.requirements
		
		// setupFile
		assert new File(python.sourceDir, 'setup.py') == python.setupFile
		python.setupFile = 'bar/setup.py'
		assert project.file('bar/setup.py') == python.setupFile
		python.setupFile = new File(python.sourceDir, 'setup.py')
		
		// sourceDir
		assert project.file('src/main/python3') == python.sourceDir
		python.sourceDir = 'src'
		assert project.file('src') == python.sourceDir
		
		// sourceFiles
		python.sourceDir = project.rootDir
		def srcFiles = python.sourceFiles as Set
		assert project.file('setup.py') in srcFiles
		assert project.file('simple/__init__.py') in srcFiles
		python.sourceFiles 'foo.bar'
		assert project.file('foo.bar') in (python.sourceFiles as Set)
		python.sourceFiles = []
		assert [] == python.sourceFiles as List
		python.sourceFiles = srcFiles as List
		
		// testDir
		assert python.sourceDir == python.testDir
		python.testDir = 'my-tests'
		assert project.file('my-tests') == python.testDir
		python.testDir = python.sourceDir
		
		// venvDir
		assert new File(python.buildDir, 'venv') == python.venvDir
		assert python.venv.rootDir == python.venvDir
		python.venvDir = 'python-venv'
		assert project.file('python-venv') == python.venvDir
		assert python.venv.rootDir == python.venvDir
		python._venvDir = new File(python.buildDir, 'venv')
		
		// version
		assert '1.2' == python.version
		project.version = '1.2-SNAPSHOT'
		assert '1.2.dev0' == python.version
		python.version = '1.2.3'
		assert '1.2.3' == python.version
		project.version = '1.2'
		python.version = null
		
		// versionFile
		assert null == python.versionFile
		python.versionFile = 'foo/version.py'
		assert project.file('foo/version.py') == python.versionFile
		python._versionFile = null
	}
}
