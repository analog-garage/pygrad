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

import java.util.concurrent.Callable

import static com.analog.garage.pygrad.LazyPropertyUtils.*

import org.gradle.api.DefaultTask
import org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
import org.junit.Test

/**
 * Tests methods in {@link LazyPropertyUtils}
 * <p>
 * @author Christopher Barber
 */
class TestLazyPropertyUtils {

	@Test
	void testAddToListFromVarargs() {
		def List<Object> list = []
		addToListFromVarargs(list, 'one', 'two')
		assert list == ['one', 'two']
		
		addToListFromVarargs(list, ['three', 'four'])
		assert list == ['one', 'two', 'three', 'four']
		
		addToListFromVarargs(list, 'five')
		assert list == ['one', 'two', 'three', 'four', 'five']
		
		list = []
		addToListFromVarargs(list)
		assert list == []
		addToListFromVarargs(list, ['a','b'], 'c')
		assert list == [['a','b'], 'c']
		
		list = []
		addToListFromVarargs1(list, 1)
		assert list == [1]
		addToListFromVarargs1(list, 2, 3, 4)
		assert list == [1,2,3,4]
		addToListFromVarargs1(list, [5,6])
		assert list == [1,2,3,4,5,6]
	}
	
	@Test
	void testIsOnWindows() {
		def osname = System.properties['os.name']
		try {
			System.setProperty 'os.name', 'Windows'
			assert isOnWindows()
			System.setProperty 'os.name', 'Darwin'
			assert !isOnWindows()
		}
		finally  {
			System.setProperty 'os.name', osname
		}
	}
	
	@Test
	void testResolveCallable() {
		def s = 'hi'
		assertSame s, resolveCallable(s)
		assertSame s, resolveCallable({s})
		assertSame s, resolveCallable(new Callable() {
			Object call() { s }
		})
		assert 'hi there' == resolveCallable( { s + ' there' } )
	}
	
	@Test
	void testResolveFile() {
		def base = new File('base')
		assert new File(base, 'bar') == resolveFile(base, 'bar')
		assert new File('bar').absoluteFile == resolveFile(base, new File('bar').absolutePath)
		assert new File('bar') == resolveFile(base, new File('bar'))
		assert new File('barfoo') == resolveFile(base, { new File('barfoo') })
	}
	
	@Test
	void testStringify() {
		assert '42' == stringify(42)
		def s = 'hi'
		assertSame s, stringify(s)
		assert 'hi there' == stringify({ s + ' there' })
		assert 'hi there' == stringify("$s there")
		assert null == stringify(null)
		assert null == stringify({ null })
	}
	
	@Test
	void testStringifyWithDefaults() {
		def extra = new DefaultExtraPropertiesExtension()
		extra.foo = 'bar'
		assert extra.has('foo')
		
		assert 'bar' == stringifyWithDefaults(null, extra, 'foo', 'barf')
		assert '42' == stringifyWithDefaults(42, extra, 'foo', 'barf')
		assert 'barf' == stringifyWithDefaults(null, extra, 'foot', 'barf')
	}
	
	@Test
	void testStringifyList() {
		assert ['1','2','3'] == stringifyList(['1',2,{ null }, { 1+ 2 }])
	}

		@Test
	void testStringifySet() {
		assert ['1','2','3'] as Set == stringifySet(['1',2,{null}, { 1+ 2 }])
	}
}
