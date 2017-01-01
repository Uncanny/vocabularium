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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Test;

import io.uncanny.vocabularium.vocabulary.Namespace;

public class NamespaceTest {

	private static final String HASH_CANONICAL="http://www.smartdeveloperhub.org/vocabulary/hash";
	private static final String HASH_ALTERNATIVE="http://www.smartdeveloperhub.org/vocabulary/hash#";
	private static final String SLASH="http://www.smartdeveloperhub.org/vocabulary/slash/";

	@Test
	public void hashNamespaceInvariantForCanonicalForm() throws Exception {
		final Namespace sut = Namespace.create(HASH_CANONICAL);
		assertThat(sut.uri(),equalTo(HASH_CANONICAL));
		assertThat(sut.canonicalForm(),equalTo(sut.uri()));
		assertThat(sut.variants(),hasItems(HASH_ALTERNATIVE,HASH_CANONICAL));
		assertThat(sut.variants(),hasSize(2));
	}

	@Test
	public void hashNamespaceInvariantForAlternativeForm() throws Exception {
		final Namespace sut = Namespace.create(HASH_ALTERNATIVE);
		assertThat(sut.uri(),equalTo(HASH_ALTERNATIVE));
		assertThat(sut.canonicalForm(),equalTo(HASH_CANONICAL));
		assertThat(sut.variants(),hasItems(HASH_ALTERNATIVE,HASH_CANONICAL));
		assertThat(sut.variants(),hasSize(2));
	}

	@Test
	public void slashNamespaceInvariant() throws Exception {
		final Namespace sut = Namespace.create(SLASH);
		assertThat(sut.uri(),equalTo(SLASH));
		assertThat(sut.canonicalForm(),equalTo(sut.uri()));
		assertThat(sut.variants(),hasItems(SLASH));
		assertThat(sut.variants(),hasSize(1));
	}

	@Test
	public void hashNamespacesWithSameCanonicalFormAreEqual() {
		final Namespace one = Namespace.create(HASH_CANONICAL);
		final Namespace other = Namespace.create(HASH_ALTERNATIVE);
		assertThat(one,equalTo(other));
	}

	@Test
	public void slashNamespacesWithSameURIAreEqual() {
		final Namespace one = Namespace.create(SLASH);
		final Namespace other = Namespace.create(SLASH);
		assertThat(one,equalTo(other));
	}

}
