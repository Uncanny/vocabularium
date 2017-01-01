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
package io.uncanny.vocabularium.handlers;

import java.net.URI;

import org.ldp4j.http.Variant;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

public final class Attachments {

	private static final AttachmentKey<URI>             BASE       = AttachmentKey.create(URI.class);
	private static final AttachmentKey<ProxyResolution> RESOLUTION = AttachmentKey.create(ProxyResolution.class);
	private static final AttachmentKey<Variant>         VARIANT    = AttachmentKey.create(Variant.class);

	private Attachments() {
	}

	public static void setVariant(final HttpServerExchange exchange, final Variant variant) {
		exchange.putAttachment(VARIANT, variant);
	}

	public static Variant getVariant(final HttpServerExchange exchange) {
		return exchange.getAttachment(VARIANT);
	}

	public static void setResolution(final HttpServerExchange exchange, final ProxyResolution resolution) {
		exchange.putAttachment(RESOLUTION, resolution);
	}

	public static ProxyResolution getResolution(final HttpServerExchange exchange) {
		return exchange.getAttachment(RESOLUTION);
	}

	public static void setBase(final HttpServerExchange exchange, final URI base) {
		exchange.putAttachment(BASE, base);
	}

	public static URI getBase(final HttpServerExchange exchange) {
		return exchange.getAttachment(BASE);
	}

}
