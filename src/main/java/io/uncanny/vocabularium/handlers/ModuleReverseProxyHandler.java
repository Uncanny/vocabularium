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

import org.ldp4j.xml.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.uncanny.vocabularium.handlers.ProxyResolution.Builder;
import io.uncanny.vocabularium.util.Location;
import io.uncanny.vocabularium.util.Tracing;
import io.uncanny.vocabularium.vocabulary.Catalog;
import io.uncanny.vocabularium.vocabulary.Module;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

final class ModuleReverseProxyHandler implements HttpHandler {

	private static final Logger LOGGER=LoggerFactory.getLogger(ModuleReverseProxyHandler.class);

	private final Catalog catalog;
	private final HttpHandler next;

	ModuleReverseProxyHandler(final Catalog catalog, final HttpHandler aHandler) {
		this.catalog = catalog;
		this.next=aHandler;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		final ProxyResolution resolution = resolveRequest(exchange);
		if(resolution==null) {
			LOGGER.debug("Accessing {} --> NOT FOUND",exchange.getRelativePath());
			exchange.setStatusCode(StatusCodes.NOT_FOUND);
			exchange.endExchange();
		} else if(resolution.target().isExternal()){
			LOGGER.debug("Accessing {} --> {} [{}] --> NOT FOUND",exchange.getRelativePath(),Tracing.describe(resolution),Tracing.catalogEntry(resolution.target()));
			exchange.setStatusCode(StatusCodes.NOT_FOUND);
			exchange.endExchange();
		} else {
			LOGGER.debug("Accessing {} --> {} [{}]",exchange.getRelativePath(),Tracing.describe(resolution),Tracing.catalogEntry(resolution.target()));
			Attachments.setResolution(exchange,resolution);
			Attachments.setBase(exchange,this.catalog.getBase());
			this.next.handleRequest(exchange);
		}
	}

	private ProxyResolution resolveRequest(final HttpServerExchange exchange) {
		final Builder builder=
			ProxyResolution.
				builder(targetRequestURI(exchange));
		final URI canonicalURI = canonicalURI(exchange);
		Module module=this.catalog.resolve(canonicalURI);
		if(module!=null) {
			return
				builder.
					resolved(canonicalURI).
					module(module).
					build();
		}
		final URI canonicalURIParent=canonicalURI.resolve(".");
		if(!canonicalURIParent.equals(canonicalURI)) {
			final String term=getTerm(exchange);
			if(XMLUtils.isNCName(term)) {
				module=this.catalog.resolve(canonicalURIParent);
				if(module!=null) {
					// TODO: Enforce that the term EXISTS in the module
					return
						builder.
							resolved(canonicalURIParent).
							module(module).
							fragment(term).
							build();
				}
			}
		}
		return null;
	}

	private URI targetRequestURI(final HttpServerExchange exchange) {
		return rebase(URI.create(exchange.getRequestURI()));
	}

	private URI canonicalURI(final HttpServerExchange exchange) {
		return resolve(exchange.getRelativePath().substring(1));
	}

	private String getTerm(final HttpServerExchange exchange) {
		final URI targetURI = URI.create(exchange.getRequestURI());
		final URI parentURI = targetURI.resolve(".");
		return parentURI.relativize(targetURI).toString();
	}

	private URI resolve(final String path) {
		return this.catalog.getBase().resolve(path);
	}

	private URI rebase(final URI uri) {
		return Location.rebase(this.catalog.getBase(), uri);
	}

}