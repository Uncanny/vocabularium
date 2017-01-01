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
package io.uncanny.vocabularium;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.undertow.util.PathMatcher;
import io.undertow.util.PathMatcher.PathMatch;

public class PathMatcherTest {

	@Test
	public void handler() {
		@SuppressWarnings("unchecked")
		final PathMatcher<String> pm=new PathMatcher<String>().
			addPrefixPath("/vocabulary/assets/","Vocab asset").
			addExactPath("/vocabulary/","Vocab page").
			addExactPath("/vocabulary/html/v1/sdh/index.html","Specific module version documentation").
			addPrefixPath("/vocabulary/html/v1/sdh/","Specific module version documentation related asset").
			addExactPath("/vocabulary/html/sdh/index.html","Specific canonical module documentation redirection").
			addPrefixPath("/vocabulary","Module implementation resolver");

		final ImmutableList<String> tests=
			ImmutableList.
				<String>builder().
					add("/vocabulary/sdh.ttl").
					add("/vocabulary/").
					add("/vocabulary/v1/sdh").
					add("/vocabulary/assets/css/vocab.css").
					add("/vocabulary/html/v1/sdh/index.html").
					add("/vocabulary/html/v1/sdh/js/bootstrap.js").
					add("/vocabulary/html/sdh/index.html").
					add("/vocabulary/html/sdh/").
					add("/vocabulary/html/v1/sdh/").
					build();
		for(final String test:tests) {
			final PathMatch<String> match = pm.match(test);
			System.out.printf("%s --[%s : %s]--> %s%n",test,match.getMatched(),match.getRemaining(),match.getValue());
		}

	}

}
