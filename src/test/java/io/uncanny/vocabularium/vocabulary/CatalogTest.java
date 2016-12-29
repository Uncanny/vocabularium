/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Uncanny Vocabularium project:
 *     http://uncanny.io/vocabularium/
 *
 *   Uncanny Software Projects
 *     http://uncanny.io/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2016 Uncanny Software Projects.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.uncanny.vocabularium.vocabulary.Catalog;
import io.uncanny.vocabularium.vocabulary.Module;
import io.uncanny.vocabularium.vocabulary.Result;

public class CatalogTest {

	@Rule
	public TestName name=new TestName();

	private void showResult(final Result<Module> result) {
		System.out.println(this.name.getMethodName());
		System.out.printf("Result:%n%s%n",result);
	}

	@Test
	public void testLoadModule$anonymousOntology() throws Exception {
		final Result<Module> result = loadModule("anon_ontology.ttl");
		showResult(result);
		assertThat(result.isAvailable(),equalTo(false));
	}

	@Test
	public void testLoadModule$badVersionInfo() throws Exception {
		final Result<Module> result = loadModule("versioned_module_with_bad_version_info.ttl");
		showResult(result);
		assertThat(result.isAvailable(),equalTo(true));
	}

	@Test
	public void testLoadModule$multipleVersionInfo() throws Exception {
		final Result<Module> result = loadModule("versioned_module_with_multiple_version_info.ttl");
		showResult(result);
		assertThat(result.isAvailable(),equalTo(true));
	}

	@Test
	public void testLoadModule$versionInfoNotAllowed() throws Exception {
		final Result<Module> result = loadModule("unversioned_module_with_version_info.ttl");
		showResult(result);
		assertThat(result.isAvailable(),equalTo(true));
	}

	private Catalog catalog() {
		return new Catalog(TestHelper.VALIDATION_CONTEXT);
	}

	private Result<Module> loadModule(final String moduleName) {
		return
			catalog().
				loadModule(
					TestHelper.
						moduleLocation(
							TestHelper.VALIDATION_CONTEXT,
							moduleName));
	}

}
