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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import io.uncanny.vocabularium.vocabulary.Catalog;
import io.uncanny.vocabularium.vocabulary.Catalogs;
import io.uncanny.vocabularium.vocabulary.Module;
import io.uncanny.vocabularium.vocabulary.Modules;
import io.uncanny.vocabularium.vocabulary.Result;

public class ModulesTest {

	private Path base(String... segments) {
		int count = TestHelper.TEST_ROOT.getNameCount();
		String[] path=new String[count+segments.length-1];
		for(int i=1;i<count;i++) {
			path[i-1]=TestHelper.TEST_ROOT.getName(i).toString();
		}
		for(int i=0;i<segments.length;i++) {
			path[count+i-1]=segments[i];
		}
		return Paths.get(TestHelper.TEST_ROOT.getName(0).toString(),path);
	}

	@Test
	public void validatesProjectVocabularies() throws Exception {
		final Result<Catalog> result = Catalogs.loadFrom(TestHelper.TEST_ROOT,TestHelper.BASE);
		if(result.isAvailable()) {
			final Catalog catalog=result.get();
			for(final String moduleId:catalog.modules()) {
				final Module module=catalog.get(moduleId);
				if(module.isExternal()) {
					assertThat("External module "+moduleId+" should be well located",Modules.isLocatedProperly(module),equalTo(true));
				} else {
					assertThat("Local module "+moduleId+" is not located properly",Modules.isLocatedProperly(module),equalTo(true));
				}
			}
		}
	}

	@Test
	public void validatesHashNamespaceNonVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.TEST_CONTEXT).
				withLocation(base("test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test#");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void validatesHashNamespaceVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.TEST_CONTEXT).
				withLocation(base("test","v1","test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test#").
				withVersionIRI("http://www.smartdeveloperhub.org/vocabulary/test/v1/test#");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void validatesSlashNamespaceNonVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.TEST_CONTEXT).
				withLocation(base("test","index.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void validatesSlashNamespaceVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.TEST_CONTEXT).
				withLocation(base("test","v1","index.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/").
				withVersionIRI("http://www.smartdeveloperhub.org/vocabulary/test/v1/");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void rejectsSlashNamespaceNonVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.TEST_CONTEXT).
				withLocation(base("test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/");
		assertThat(Modules.isLocatedProperly(module),equalTo(false));
	}

	@Test
	public void rejectsSlashNamespaceVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.TEST_CONTEXT).
				withLocation(base("test","v1","test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/").
				withVersionIRI("http://www.smartdeveloperhub.org/vocabulary/test/v1/");
		assertThat(Modules.isLocatedProperly(module),equalTo(false));
	}

}
