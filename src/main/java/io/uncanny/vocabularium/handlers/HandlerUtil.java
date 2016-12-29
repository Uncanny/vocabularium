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
package io.uncanny.vocabularium.handlers;

import java.net.URI;

import io.undertow.server.HttpServerExchange;

final class HandlerUtil {

	private HandlerUtil() {
	}

	static String getExtension(final String module) {
		final int lastSlashIndex=module.lastIndexOf('/');
		if(lastSlashIndex==-1) {
			return "";
		}
		final int dotIndex=module.lastIndexOf('.',lastSlashIndex);
		if(dotIndex==-1) {
			return "";
		}
		return module.substring(dotIndex + 1);
	}

	static URI canonicalURI(final HttpServerExchange exchange) {
		final String relativePath = exchange.getRelativePath();
		final String normalizedRelativePath = relativePath.isEmpty()?relativePath:relativePath.substring(1);
		return getBase(exchange).resolve(normalizedRelativePath);
	}

	static URI canonicalURI(final HttpServerExchange exchange, final String path) {
		return getBase(exchange).resolve(path);
	}

	private static URI getBase(final HttpServerExchange exchange) {
		URI base=Attachments.getBase(exchange);
		if(base==null) {
			base=physicalBase(exchange);
		}
		return base;
	}

	private static URI physicalBase(final HttpServerExchange exchange) {
		return URI.create(String.format("%s://%s%s/",exchange.getRequestScheme(),exchange.getHostAndPort(),exchange.getResolvedPath()));
	}

}
