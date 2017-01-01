/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Uncanny Vocabularium project:
 *     http://uncanny.io/vocabularium/
 *
 *   Uncanny Software Projects
 *     http://uncanny.io/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2016-2017 Uncanny Software Projects.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package io.uncanny.vocabularium.vocabulary;

final class StringUtil {

	private StringUtil() {
	}

	static String indent(final String string, final String separator) {
		final String t1 = "T1["+string.hashCode()+"]";
		final String t2 = "T2["+string.hashCode()+"]";
		final String r1 = string.replace("\r\n",t1+separator);
		final String r2 =     r1.replace("\n"  ,t2+separator);
		final String r3 =     r2.replace(t1,"\r\n");
		return                r3.replace(t2,"\n");
	}

}
