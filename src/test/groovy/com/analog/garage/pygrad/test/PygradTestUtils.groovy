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

import java.nio.file.Paths

import javax.swing.plaf.basic.BasicFileChooserUI.NewFolderAction

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import groovy.sql.Sql.CreateCallableStatementCommand

/**
 * @author Christopher Barber
 */
class PygradTestUtils {
	
	/**
	 * Recursively copies directory {@code fromDir} to {@code toDir}
	 */
	static void copyDir(fromDir, toDir) {
		new AntBuilder().copy(todir: toDir) {
			fileset(dir: fromDir)
		}
	}
	
	/**
	 * Copies test project into subdirectory of tmpFolder
	 * @param projectName
	 * @param tmpFolder
	 * @return location of destination directory
	 */
	static File copyTestProject(String projectName, TemporaryFolder tmpFolder) {
		File projectDir = tmpFolder.newFolder(projectName)
		copyDir(testProjectDir(projectName), projectDir)
		return projectDir
	}
	
	/**
	 * Default version of gradle to test against.
	 * <p>
	 * This is the version of Gradle this library is compiled against.
	 */
	static String defaultGradleVersion() {
		ProjectBuilder.builder().build().gradle.gradleVersion
	}

	/**
	 * Root directory of this project.
	 * <p>
	 * This is based on the 'user.dir' Java property, which appears to be set to the project
	 * root dir in JUnit under Eclipse and when run from gradle.
	 * @return
	 */
	static File projectDir() {
		new File(System.getProperty('user.dir'))
	}
	
	static String pythonExePropertyArg() {
		def exePath = "which python3".execute().text
		println '*** ' + exePath
		'-Pcom.analog.garage.pygrad.pythonExe=' + exePath
	}
	
	/**
	 * 
	 */
	static GradleRunner testGradleRunner(String projectName, TemporaryFolder tmpFolder) {
		
		GradleRunner.create()
		    .withGradleVersion(defaultGradleVersion())
			.withProjectDir(copyTestProject(projectName, tmpFolder))
			.withTestKitDir(tmpFolder.newFolder('gradle-test-kit'))
			.withPluginClasspath()
	}
	
	/**
	 * Find directory containing source of test project with given name.
	 */
	static File testProjectDir(String projectName) {
		projectDir().toPath().resolve('src/test/projects/' + projectName).toFile()
	}
	
	/**
	 * Configures {@link ProjectBuilder} for given test project.
	 */
	static ProjectBuilder testProjectBuilder(String projectName, TemporaryFolder tmpFolder) {
		ProjectBuilder.builder()
			.withName(projectName)
			.withProjectDir(copyTestProject(projectName, tmpFolder));
	}
}
