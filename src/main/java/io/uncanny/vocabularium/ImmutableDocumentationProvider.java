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

import java.nio.file.Path;

import io.uncanny.vocabularium.spi.DocumentationProvider;
import io.uncanny.vocabularium.vocabulary.Module;

final class ImmutableDocumentationProvider implements DocumentationProvider {

	private final Path assetsPath;

	public ImmutableDocumentationProvider(final Path assetsPath) {
		this.assetsPath = assetsPath;
	}

	@Override
	public Path assetsPath() {
		return this.assetsPath;
	}

	static ImmutableDocumentationProvider create(final DocumentationStrategy strategy, final Module module) {
		final Path path =
			strategy.
				assetsPath(
					module.context().base(),
					module.implementationIRI());
		return new ImmutableDocumentationProvider(path);
	}

}
