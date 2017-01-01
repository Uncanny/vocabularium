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
package io.uncanny.vocabularium.util;

import static org.ldp4j.net.URI.wrap;

import java.net.URI;
import java.net.URISyntaxException;

public final class Location {

	private Location() {
	}

	public static URI rebase(final URI base, final URI uri) throws AssertionError {
		try {
			return
				new URI(
					base.getScheme(),
					base.getAuthority(),
					uri.getPath(),
					uri.getQuery(),
					uri.getFragment());
		} catch (final URISyntaxException e) {
			throw new AssertionError("Rebasing of '"+uri+"' according to '"+base.getScheme()+"://"+base.getAuthority()+"' should not fail",e);
		}
	}

	public static URI relativize(final URI base, final URI source, final URI target) throws AssertionError {
		final URI rebased=rebase(base,source);
		return wrap(rebased).relativize(wrap(target)).unwrap();
	}

}
