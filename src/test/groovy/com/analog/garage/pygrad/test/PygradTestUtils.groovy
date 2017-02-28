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
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner

import groovy.sql.Sql.CreateCallableStatementCommand

/**
 * @author Christopher Barber
 */
class PygradTestUtils {

	/**
	 * 
	 */
	static GradleRunner testGradleRunner(String projectName) {
		// By default use gradle version we compiled against.
		def version = ProjectBuilder.builder().build().gradle.gradleVersion
		GradleRunner.create()
		    .withGradleVersion(version)
			.withProjectDir(testProjectDir(projectName))
			.withPluginClasspath()
	}
	
	/**
	 * Find directory containing test project with given name.
	 */
	static File testProjectDir(String projectName) {
		// FIXME - 
		return Paths.get(PygradTestUtils.getResource('/projects/' + projectName).toURI()).toFile()
	}
	
	/**
	 * Configures {@link ProjectBuilder} for given test project.
	 */
	static ProjectBuilder testProjectBuilder(String projectName) {
		ProjectBuilder.builder()
			.withName(projectName)
			.withProjectDir(testProjectDir(projectName));
	}
}
