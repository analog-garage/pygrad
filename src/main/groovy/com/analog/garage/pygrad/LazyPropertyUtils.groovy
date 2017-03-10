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

import java.io.File
import java.util.concurrent.Callable
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * Static methods that can be used in implementation of lazy properties.
 * <p>
 * @author Christopher Barber
 */
class LazyPropertyUtils 
{
	/**
	 * Appends objects to list from variable length argument list.
	 * <p>
	 * @param list is the list to which objects will be added
	 * @param varargs will be added to the list, but if it has only
	 * a single entry that is itself a {@link Collection},
	 * all of its contents will be added instead of the list itself.
	 */
	@SafeVarargs
	static <T> void addToListFromVarargs(List<T> list, T ... varargs) {
		if (varargs.length == 1 && varargs[0] instanceof Collection)
			list.addAll(varargs[0])
		else
			for (obj in varargs)
				list.add(obj)
	}

	/**
	 * Appends objects to list from variable length argument list.
	 * <p>
	 * @param list is the list to which objects will be added
	 * @param first is the first object to add to the list. If there are no
	 * additional arguments and this is itself a {@link Collection},
	 * all of its contents will be added instead of the list itself.
	 * @param more are additional objects to add.
	 */
	@SafeVarargs
	static <T> void addToListFromVarargs1(List<T> list, T first, T ... more) {
		if (more.length == 0 && first instanceof Collection)
			list.addAll(first)
		else
			list.add(first)
		for (obj in more)
			list.add(obj)
	}

	/**
	 * True is system properties indicates a variant of the Windows operating system.
	 */
	static boolean isOnWindows() {
		System.getProperty('os.name').toLowerCase().contains('windows')
	}
	
	/**
	 * Returns object or callable result.
	 * <p>
	 * If {@code obj} is a {@link Callable}, returns its result, otherwise
	 * returns {@code obj}.
	 */
	static Object resolveCallable(Object obj) {
		obj instanceof Callable ? obj.call() : obj
	}
	
	/**
	 * Resolves path relative to root file.
	 * @param root
	 * @param path if a {@link Callable} (or closure) replaces with the call result,
	 * then resolves relative to the root directory
	 */
	static File resolveFile(File root, Object path) {
		path = resolveCallable(path)
		if (path instanceof File)
			return path
		return root.toPath().resolve(path).toFile();
	}

	/**
	 * Converts object to string.
	 * <p>
	 * If {@code obj} is a {@link Callable}, replaces it with the call result.
	 * Then returns result of {@link Object#toString} method or null if call result was null.
	 */
	static String stringify(Object obj) {
		resolveCallable(obj)?.toString()
	}
	
	/**
	 * Converts value to string or looks up from properties.
	 * <p>
	 * If {@code value} is null, it will be replaced by value of {@code propertyName} property
	 * in {@code ext} or {@code defaultValue} if there is no such property or the property
	 * has a null value. Returns the result of applying {@link #stringify(Object)} to this value.
	 */
	static String stringifyWithDefaults(
		Object value, 
		ExtraPropertiesExtension ext, 
		String propertyName, 
		Object defaultValue)
	{
		if (value == null) {
			if (ext.has(propertyName))
				value = ext.get(propertyName)
			if (value == null)
				value = defaultValue
		}
		return stringify(value)
	}
	
	/**
	 * Converts objects to set of strings.
	 * <p>
	 * Returns a new set resulting from converting each object using
	 * {@link #stringify}, but excluding any null entries.
	 */
	static List<String> stringifyList(Iterable<Object> objects) {
		def newList = new ArrayList<String>()
		for (obj in objects) {
			def s = stringify(obj)
			if (s != null)
				newList.add(s)
		}
		return newList
	}
	
	/**
	 * Converts objects to set of strings.
	 * <p>
	 * Returns a new set resulting from converting each object using
	 * {@link #stringify}, but excluding any null entries.
	 */
	static Set<String> stringifySet(Iterable<Object> objects) {
		def newSet = new LinkedHashSet<String>(objects.size())
		for (obj in objects) {
			def s = stringify(obj)
			if (s != null)
				newSet.add(s)
		}
		return newSet
	}
}
