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

package com.analog.garage.pygrad.test;

import static org.junit.Assert.*

import org.junit.Rule

import static PygradTestUtils.*

import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.analog.garage.pygrad.DevpiTaskBase

import org.gradle.api.plugins.buildcomparison.outcome.internal.BuildOutcome
import org.gradle.internal.impldep.org.apache.maven.execution.BuildSuccess
import org.gradle.testkit.runner.TaskOutcome

/**
 * @author Christopher Barber
 */
class TestPythonPlugin extends PygradTestBase {

	/**
	 * Test plugin on 'simple' test project
	 */
	@Test
	public void testSimple() {
		def runner = testGradleRunner('simple', folder)
		def result = runner.withArguments('pyenv').build()
		
		assert result.task(':pyenv').outcome == TaskOutcome.SUCCESS

		result = runner.withArguments('pydist').forwardOutput().build()
		
		assert result.task(':pyenv').outcome == TaskOutcome.UP_TO_DATE
		assert result.task(':pyversionfile').outcome == TaskOutcome.SUCCESS
		assert result.task(':pysources').outcome == TaskOutcome.SUCCESS
		assert result.task(':pydist').outcome == TaskOutcome.SUCCESS
		assert new File(runner.projectDir, 'build/python/dist/simple-1.2.dev0.zip').exists()
	}

}
