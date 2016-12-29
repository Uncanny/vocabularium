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

import java.net.URI;
import java.util.List;

import org.junit.Test;

import io.uncanny.vocabularium.vocabulary.Catalog;
import io.uncanny.vocabularium.vocabulary.Catalogs;
import io.uncanny.vocabularium.vocabulary.Module;
import io.uncanny.vocabularium.vocabulary.Result;
import io.uncanny.vocabularium.vocabulary.VocabularyHelper;

public class VocabularyHelperTest {

	@Test
	public void testClasses() throws Exception {
		final Result<Catalog> result=
			Catalogs.
				loadFrom(
					TestHelper.TEST_ROOT,
					TestHelper.BASE);
		final Catalog catalog=result.get();
		final Module ci = catalog.resolve(URI.create("v1/ci"));
		final VocabularyHelper helper = VocabularyHelper.create(ci);
		System.out.println("Namespace prefixes: "+helper.namespacePrefixes());
		System.out.println("Prefixes: "+helper.prefixes());
		dumpValues("classes", helper.classes());
		dumpValues("object properties", helper.objectProperties());
		dumpValues("datatype properties", helper.datatypeProperties());
		dumpValues("individuals", helper.individuals());
		dumpValues("URIrefs (http://www.smartdeveloperhub.org/vocabulary/ci#)", helper.uriRefs("http://www.smartdeveloperhub.org/vocabulary/ci#"));
	}

	private void dumpValues(final String type, final List<String> values) {
		if(values.isEmpty()) {
			System.out.printf("No %s available%n",type);
		} else {
			System.out.printf("%d %s available%n",values.size(),type);
			for(final String value:values) {
				System.out.printf("- %s%n",value);
			}
		}
	}

}
