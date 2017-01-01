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

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;

public class TestHelper {

	public static final URI     BASE = URI.create("http://www.smartdeveloperhub.org/vocabulary/");

	public static final Path    TEST_ROOT       = Paths.get("src","test","resources","scenarios","test");
	public static final Path    VALIDATION_ROOT = Paths.get("src","test","resources","modules");

	public static final Context TEST_CONTEXT       = Context.create(BASE,TEST_ROOT);
	public static final Context VALIDATION_CONTEXT = Context.create(BASE,VALIDATION_ROOT);

	public static Model load(final Context context, final String relativePath) throws IOException {
		final Path file=moduleLocation(context,relativePath);
		final Model model=ModelFactory.createDefaultModel();
		final RDFReader reader=model.getReader("TURTLE");
		reader.setProperty("error-mode", "strict-fatal");
		reader.
			read(
				model,
				new FileReader(
					file.toFile()),
					context.getCanonicalNamespace(file).toString());
		return model;
	}

	public static String uriRef(final String ontology, final String localPart) {
		return BASE.resolve(ontology+"#"+localPart).toString();
	}

	public static Path moduleLocation(final Context context, final String name) {
		return context.root().resolve(name);
	}

}
