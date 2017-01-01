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
package io.uncanny.vocabularium;

import org.ldp4j.http.MediaType;
import org.ldp4j.http.MediaTypes;

import io.uncanny.vocabularium.vocabulary.Module.Format;

public final class Formats {

	private static final MediaType JSON_LD = MediaTypes.of("application","ld","json");
	private static final MediaType RDF_XML = MediaTypes.of("application","rdf","xml");
	private static final MediaType TURTLE = MediaTypes.of("text","turtle");

	private Formats() {
	}

	public static MediaType toMediaType(final Format format) {
		MediaType result=null;
		if(Format.TURTLE.equals(format)) {
			result=TURTLE;
		} else if(Format.RDF_XML.equals(format)) {
			result=RDF_XML;
		} else if(Format.JSON_LD.equals(format)) {
			result=JSON_LD;
		} else if(format!=null){
			throw new IllegalArgumentException("Unsupported format '"+format+"'");
		}
		return result;
	}

	public static Format fromMediaType(final MediaType type) {
		Format result=null;
		if("application".equals(type.type()) && "rdf".equals(type.subType()) && "xml".equals(type.suffix())) {
			result=Format.RDF_XML;
		} else if("text".equals(type.type()) && "turtle".equals(type.subType())) {
			result=Format.TURTLE;
		} else if("application".equals(type.type()) && "ld".equals(type.subType()) && "json".equals(type.suffix())) {
			result=Format.JSON_LD;
		} else if(type!=null) {
			throw new IllegalArgumentException("Unsupported media type '"+type.toHeader()+"'");
		}
		return result;
	}

}
