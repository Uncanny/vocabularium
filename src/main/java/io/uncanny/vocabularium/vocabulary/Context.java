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

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import com.google.common.base.MoreObjects;

public final class Context {

	private final URI base;
	private final Path root;

	private Context(final URI base, final Path root) {
		this.base = base;
		this.root = root;
	}

	public URI base() {
		return this.base;
	}

	public Path root() {
		return this.root;
	}

	boolean includesNamespace(final String ontology) {
		return !this.base.relativize(URI.create(ontology)).isAbsolute();
	}

	String getRelativePath(final Path file) {
		final Path absoluteBasePath = file.getParent().resolve(MorePaths.getFileName(file));
		final Path relativeBasePath = this.root.relativize(absoluteBasePath);
		return relativeBasePath.toString().replace(File.separatorChar, '/');
	}

	URI getCanonicalNamespace(final Path file) {
		return this.base.resolve(getRelativePath(file));
	}

	String getImplementationPath(final Path file) {
		final Path relativeBasePath = this.root.relativize(file);
		return relativeBasePath.toString().replace(File.separatorChar, '/');
	}

	URI getImplementationEndpoint(final Path file) {
		return this.base.resolve(getImplementationPath(file));
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("base",this.base).
					add("root",this.root).
					toString();
	}

	static Context create(final URI base, final Path root) {
		return new Context(base,root);
	}

}
