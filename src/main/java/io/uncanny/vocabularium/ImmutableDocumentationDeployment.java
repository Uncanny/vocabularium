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
package io.uncanny.vocabularium;

import java.net.URI;

import io.uncanny.vocabularium.spi.DocumentationDeployment;
import io.uncanny.vocabularium.vocabulary.Module;

class ImmutableDocumentationDeployment implements DocumentationDeployment {

	private final Module module;
	private final URI implementationRoot;
	private final URI implementationLandingPage;
	private final URI canonicalLandingPage;

	private ImmutableDocumentationDeployment(final Module module, final String moduleDocumentationRoot,final String canonicalDocumentationRoot, final String landingPage) {
		final URI base=module.context().base();
		this.module=module;
		this.implementationRoot=base.resolve(moduleDocumentationRoot);
		this.implementationLandingPage=base.resolve(moduleDocumentationRoot+landingPage);
		this.canonicalLandingPage=base.resolve(canonicalDocumentationRoot+landingPage);
	}

	@Override
	public Module module() {
		return this.module;
	}

	@Override
	public URI implementationRoot() {
		return this.implementationRoot;
	}

	@Override
	public URI implementationLandingPage() {
		return this.implementationLandingPage;
	}

	@Override
	public URI canonicalLandingPage() {
		return this.canonicalLandingPage;
	}

	static ImmutableDocumentationDeployment create(final DocumentationStrategy strategy, final Module module) {
		final URI base=module.context().base();
		final String moduleDocumentationRoot=strategy.rootPath(base,module.implementationIRI());
		final String canonicalDocumentationRoot=strategy.rootPath(base,module.ontology());
		return new ImmutableDocumentationDeployment(module,moduleDocumentationRoot,canonicalDocumentationRoot,"index.html");
	}

}