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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.uncanny.vocabularium.handlers.Attachments;
import io.uncanny.vocabularium.handlers.ProxyResolution;
import io.uncanny.vocabularium.spi.DocumentationDeploymentFactory;
import io.uncanny.vocabularium.util.Location;
import io.uncanny.vocabularium.util.Tracing;
import io.uncanny.vocabularium.vocabulary.Module;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

final class ModuleDocumentationRedirector implements HttpHandler {

	private static final Logger LOGGER=LoggerFactory.getLogger(ModuleDocumentationRedirector.class);

	private final DocumentationDeploymentFactory deploymentFactory;

	ModuleDocumentationRedirector(final DocumentationDeploymentFactory factory) {
		this.deploymentFactory = factory;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if(!Strings.isNullOrEmpty(exchange.getQueryString())) {
			exchange.setStatusCode(StatusCodes.BAD_REQUEST);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
			exchange.getResponseSender().send("Queries not allowed");
			return;
		}
		final URI location=getDocumentationLocation(exchange);
		final URI relativeLocation=relativize(exchange, location);
		exchange.setStatusCode(StatusCodes.SEE_OTHER);
		exchange.getResponseHeaders().clear();
		exchange.getResponseHeaders().add(Headers.LOCATION,relativeLocation.toString());
	}

	private URI getDocumentationLocation(final HttpServerExchange exchange) {
		final ProxyResolution resolution = Attachments.getResolution(exchange);
		final Module module=resolution.target();
		final URI result = this.deploymentFactory.create(module).implementationLandingPage();
		LOGGER.debug("Redirecting {} ({}) to {}",Tracing.describe(resolution),Tracing.catalogEntry(module),result);
		return result;
	}

	private URI relativize(final HttpServerExchange exchange, final URI location) {
		final ProxyResolution resolution = Attachments.getResolution(exchange);
		URI result =
			Location.relativize(
				resolution.target().context().base(),
				URI.create(exchange.getRequestURI()),
				location);
		if(resolution.isFragment()) {
			result=result.resolve("#"+resolution.fragment());
		}
		LOGGER.trace("Effective path to {} is {}",location,result);
		return result;
	}
}