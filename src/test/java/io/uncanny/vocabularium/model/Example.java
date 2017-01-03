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
package io.uncanny.vocabularium.model;

import java.util.Arrays;

import com.google.common.collect.Lists;

public class Example {

	private Example() {
	}

	public static Site site() {
		final Owner owner = owner();
		final Metadata meta = metadata();
		final Ontology ontology = ontology();
		final Site site=new Site();
		site.setTitle("uncanny.io");
		site.setCopyright("Uncanny Software Projects");
		site.setMetadata(meta);
		site.setOwner(owner);
		site.getOntologies().add(ontology);
		site.setTags(Lists.newArrayList("Uncanny","Vocabularium"));
		return site;
	}

	public static Ontology ontology() {
		final License license = license();

		final Language language = language();

		final Ontology ontology = new Ontology();
		ontology.setId("uncanny.io.vocabularium");
		ontology.setUri("http://vocabularium.uncanny.io/vocabulary");
		ontology.setTitle("Vocabularium Ontology");
		ontology.getLicenses().add(license);
		ontology.getLanguages().add(language);
		ontology.getDomains().addAll(Lists.newArrayList("Semantic Web","Vocabulary Publishing"));
		ontology.setSummary("Abbreviated description of the 'Vocabularium Ontology'");
		ontology.setDescription("Quite a long description of the 'Vocabularium Ontology'");
		return ontology;
	}

	public static Language language() {
		final Language language=new Language();
		language.setUri("http://purl.org/NET/rdflicense/APACHE2.0");
		language.setLabel("en");
		language.setName("English");
		return language;
	}

	public static License license() {
		final License license = new License();
		license.setLabel("Apache License 2.0");
		license.setUri("http://purl.org/NET/rdflicense/APACHE2.0");
		return license;
	}

	public static Metadata metadata() {
		final Metadata meta=new Metadata();
		meta.setApplicationName("Vocabularium Catalog");
		meta.setLanguage("en");
		meta.setDescription("Vocabularies of the Vocabularium project");
		meta.getAuthors().add("Miguel Esteban Gutiérrez");
		meta.getKeywords().addAll(Arrays.asList("Semantic Web","Ontology","Vocabulary"));
		return meta;
	}

	public static Owner owner() {
		final Owner owner = new Owner();
		owner.setName("Uncanny Software Projects");
		owner.setUri("http://uncanny.io");
		owner.setLogo("logos/uncanny.png");
		return owner;
	}

}
