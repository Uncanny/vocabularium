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

import static io.undertow.Handlers.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.uncanny.vocabularium.spi.DocumentationProvider;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.PathResourceManager;

final class ExternalAssetProvider implements HttpHandler {

	private static final Logger LOGGER=LoggerFactory.getLogger(ExternalAssetProvider.class);

	private final HttpHandler next;
	private final DocumentationProvider provider;

	ExternalAssetProvider(final DocumentationProvider provider) {
		this.provider = provider;
		this.next =
			resource(
				new PathResourceManager(provider.assetsPath(),100000,false,false));
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		LOGGER.debug("Retrieving asset {} from {}",exchange.getRelativePath(),this.provider.assetsPath());
		this.next.handleRequest(exchange);
		LOGGER.trace("Checked: {}", exchange.getStatusCode());
	}

}