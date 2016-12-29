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

import org.ldp4j.http.Variant;

import com.google.common.base.Strings;

import io.uncanny.vocabularium.handlers.Attachments;
import io.uncanny.vocabularium.handlers.ProxyResolution;
import io.uncanny.vocabularium.vocabulary.Module;
import io.uncanny.vocabularium.vocabulary.Namespace;
import io.uncanny.vocabularium.vocabulary.SerializationManager;
import io.uncanny.vocabularium.vocabulary.Module.Format;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

final class ModuleRepresentionGenerator implements HttpHandler {

	private final SerializationManager manager;

	ModuleRepresentionGenerator(final SerializationManager manager) {
		this.manager = manager;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if(!Strings.isNullOrEmpty(exchange.getQueryString())) {
			exchange.setStatusCode(StatusCodes.BAD_REQUEST);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
			exchange.getResponseSender().send("Queries not allowed");
		} else {
			final Variant variant=Attachments.getVariant(exchange);
			final ProxyResolution resolution = Attachments.getResolution(exchange);
			final String contentLocation=getContentLocation(resolution);
			if(contentLocation!=null) {
				exchange.getResponseHeaders().add(Headers.CONTENT_LOCATION,contentLocation);
			}
			final Module module=resolution.target();
			final Format format = Formats.fromMediaType(variant.type());
			final SerializationHandler next = new SerializationHandler(this.manager, module, format);
			next.handleRequest(exchange);
		}
	}

	private String getContentLocation(final ProxyResolution resolution) {
		String contentLocation=null;
		if(isReference(resolution)) {
			contentLocation=implementationIRI(resolution);
		}
		if(resolution.isFragment()) {
			contentLocation+="#"+resolution.fragment();
		}
		return contentLocation;
	}

	private boolean isReference(final ProxyResolution resolution) {
		return resolution.isFragment() || !resolution.resolvedURI().toString().equals(implementationIRI(resolution));
	}

	private String implementationIRI(final ProxyResolution resolution) {
		return Namespace.create(resolution.target().implementationIRI()).canonicalForm();
	}

}