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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

public final class Module {

	public enum Format {
		TURTLE(RDFLanguages.TURTLE,"ttl"),
		RDF_XML(RDFLanguages.RDFXML,"rdf"),
		JSON_LD(RDFLanguages.JSONLD,"jsonld"),
		;

		final Lang lang;
		private final String extension;

		private Format(final Lang lang,final String extension) {
			this.lang = lang;
			this.extension = extension;
		}

		public String getName() {
			return this.lang.getName();
		}

		public static Module.Format fromExtension(final String aExtension) {
			for(final Module.Format format:values()) {
				for(final String extension:format.lang.getFileExtensions()) {
					if(extension.equalsIgnoreCase(aExtension)) {
						return format;
					}
				}
			}
			return null;
		}

		static Module.Format fromMime(final String aMime) {
			final ContentType expectedCT = ContentType.create(aMime);
			for(final Module.Format format:values()) {
				final ContentType candidateCT=format.lang.getContentType();
				if(candidateCT.getType().equals(expectedCT.getType()) && candidateCT.getSubType().equals(expectedCT.getSubType())) {
					return format;
				}
			}
			return null;
		}

		public String mime() {
			final ContentType contentType = this.lang.getContentType();
			return contentType.getType()+"/"+contentType.getSubType();
		}

		public String contentType(final Charset charset) {
			final ContentType contentType = this.lang.getContentType();
			return String.format("%s/%s; charset=\"%s\"",contentType.getType(),contentType.getSubType(),charset.name());
		}

		public String fileExtension() {
			return this.extension;
		}
	}

	private final Context context;

	private Path   location;
	private Format format;

	private String relativePath;
	private URI    locationNamespace;

	private String ontology;
	private String versionIRI;
	private final Set<String> priorVersions;
	private final Set<String> imports;

	private String versionInfo;


	Module(final Context context) {
		this.context = context;
		this.imports=Sets.newLinkedHashSet();
		this.priorVersions=Sets.newLinkedHashSet();
	}

	Module withLocation(final Path value) {
		this.location=value;
		this.locationNamespace=this.context.getCanonicalNamespace(this.location);
		this.relativePath=this.context.getRelativePath(this.location);
		return this;
	}

	Module withFormat(final Format value) {
		this.format=value;
		return this;
	}

	Module withOntology(final String value) {
		this.ontology=value;
		return this;
	}

	Module withVersionIRI(final String value) {
		this.versionIRI=value;
		return this;
	}

	Module withVersionInfo(final String versionInfo) {
		this.versionInfo = versionInfo;
		return this;
	}

	Module withPriorVersions(final Collection<? extends String> priorVersions) {
		this.priorVersions.addAll(priorVersions);
		return this;
	}

	Module withImports(final Collection<? extends String> imports) {
		this.imports.addAll(imports);
		return this;
	}

	URI locationNamespace() {
		return this.locationNamespace;
	}

	boolean isCanonical() {
		return isLocal() && !isVersion();
	}

	/**
	 * NOTE: We are checking for an antipattern: it should not be the case that
	 * the ontology URI matches the versionIRI
	 */
	boolean isCanonicalVersion() {
		return isLocal() && isVersion() && !Namespace.create(this.ontology).equals(Namespace.create(this.versionIRI));
	}

	public Context context() {
		return this.context;
	}

	public Path location() {
		return this.location;
	}

	public String relativePath() {
		return this.relativePath;
	}

	public Format format() {
		return this.format;
	}

	public String ontology() {
		return this.ontology;
	}

	public String versionIRI() {
		return this.versionIRI;
	}

	/**
	 * TODO: Should we allow having versionIRI but not ontology IRI?
	 */
	public String implementationIRI() {
		String implementationIRI=this.versionIRI;
		if(implementationIRI==null) {
			implementationIRI=this.ontology;
		}
		return implementationIRI;
	}

	public Set<String> priorVersions() {
		return ImmutableSortedSet.copyOf(this.priorVersions);
	}

	public Set<String> imports() {
		return ImmutableSortedSet.copyOf(this.imports);
	}

	public boolean isLocal() {
		return
			isOntology() &&
			this.context.includesNamespace(this.ontology) &&
			(isVersion()?
				this.context.includesNamespace(this.versionIRI):
				true);
	}

	public boolean isExternal() {
		return !isLocal();
	}

	public boolean isOntology() {
		return this.ontology!=null;
	}

	public boolean isVersion() {
		return this.versionIRI!=null;
	}

	public boolean hasImports() {
		return !this.imports.isEmpty();
	}

	public String transform(final URI base, final Format format) throws IOException {
		return
			new ModuleHelper(this.location).
				load(base,this.format).
				export(format);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("context",this.context).
					add("location",this.location).
					add("relativePath",this.relativePath).
					add("locationNamespace",this.locationNamespace).
					add("format",this.format).
					add("external",isExternal()).
					add("ontology",this.ontology).
					add("versionIRI",this.versionIRI).
					add("versionInfo",this.versionInfo).
					add("priorVersions",this.priorVersions.isEmpty()?null:this.priorVersions).
					add("imports",this.imports.isEmpty()?null:this.imports).
					toString();
	}

}
