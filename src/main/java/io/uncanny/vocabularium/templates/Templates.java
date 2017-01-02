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
package io.uncanny.vocabularium.templates;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.format.DateTimeFormat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.InterpretException;

import io.uncanny.vocabularium.model.Model;
import io.uncanny.vocabularium.model.Ontology;
import io.uncanny.vocabularium.model.Site;

public final class Templates {

	private static final TypeReference<Map<String, Object>> BINDING_TYPE;
	private static final ObjectMapper MAPPER;
	private static final JinjavaConfig CONFIGURATION;
	private static final String CATALOG_TEMPLATE;

	static {
		CATALOG_TEMPLATE=loadTemplate();
		CONFIGURATION=
			JinjavaConfig.
				newBuilder().
					withLstripBlocks(true).
					withTrimBlocks(true).
					build();
		MAPPER=
			new ObjectMapper().
				enable(SerializationFeature.INDENT_OUTPUT);
		BINDING_TYPE=new TypeReference<Map<String, Object>>() {};
	}

	private Templates() {
	}

	public static String catalogRepresentation(final Site site) throws IOException {
		final Site clone=curate(site);
		final String tmp=MAPPER.writeValueAsString(clone);
		final Map<String,Object> bindings=MAPPER.readValue(tmp,BINDING_TYPE);
		return
			catalogRepresentation(
				ImmutableMap.
					<String,Object>builder().
						put("publication",bindings).
						build());
	}

	private static String catalogRepresentation(final Map<String, Object> bindings) {
		try {
			return new Jinjava(CONFIGURATION).render(CATALOG_TEMPLATE,bindings);
		} catch(final InterpretException e) {
			// TODO: This should be logged properly
			System.err.println("Template:\n"+CATALOG_TEMPLATE);
			System.err.println("Bindings:\n"+bindings);
			e.printStackTrace(System.err);
			return "<html><head></head><body><h1>OOPS! Something went wrong...</h1>Couldn't generate catalog page:<br><pre>"+Throwables.getStackTraceAsString(e)+"</pre></body></html>";
		}
	}

	private static Site curate(final Site site) {
		final Site clone=Model.clone(site);
		// TODO: What if there's no metadata, or it does not have language, or the language is not valid?
		final Locale siteLocale =
			Locale.
				forLanguageTag(
					clone.getMetadata().getLanguage());
		// TODO: Should we allow having a custom date specified or gather it from the ontologies in the catalog, i.e., that latest modification from all the modules.
		clone.setDate(
			DateTimeFormat.
				forPattern("MMMM, YYYY").
					withLocale(siteLocale).
					print(System.currentTimeMillis()));
		final Escaper escaper = HtmlEscapers.htmlEscaper();
		final List<String> tags = clone.getTags();
		for(final Ontology ontology:clone.getOntologies()) {
			// TODO: What if there's not ontology title
			tags.add(ontology.getTitle());
			for(final String domain:ontology.getDomains()) {
				// TODO: What if the domain is null
				tags.add(domain);
			}
			ontology.setSummary(escape(escaper, ontology.getSummary()));
			ontology.setDescription(escape(escaper, ontology.getDescription()));
		}
		return clone;
	}

	private static String escape(final Escaper escaper, final String text) {
		String escape=null;
		if(text!=null) {
			escape = escaper.escape(text);
		}
		return escape;
	}

	private static String loadTemplate() {
		try {
			return
				Resources.
					toString(
						Resources.getResource("vocabularium/templates/vocab.html"),
						StandardCharsets.UTF_8);
		} catch (final Exception e) {
			throw new AssertionError("Could not load catalog representation template",e);
		}
	}

}
