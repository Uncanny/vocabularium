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
package io.uncanny.vocabularium.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

public final class ConfigurationFactory {

	private ConfigurationFactory() {
	}

	private static YAMLFactory yamlFactory() {
		return
			new YAMLFactory().
				disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).
				disable(YAMLGenerator.Feature.CANONICAL_OUTPUT).
				enable(YAMLGenerator.Feature.SPLIT_LINES).
				enable(YAMLGenerator.Feature.MINIMIZE_QUOTES).
				enable(YAMLGenerator.Feature.USE_NATIVE_OBJECT_ID).
				enable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID);
	}

	private static ObjectMapper parsingMapper() {
		return new ObjectMapper(yamlFactory());
	}

	private static ObjectMapper writingMapper() {
		final YAMLFactory factory = yamlFactory();
		factory.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
		final ObjectMapper mapper = new ObjectMapper(factory);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper;
	}

	static <T extends Configuration> T load(final URL url, final Class<? extends T> clazz) throws ConfigurationException {
		final ObjectMapper mapper = parsingMapper();
		try(InputStream openStream = url.openStream()) {
			try {
				return mapper.readValue(openStream,clazz);
			} catch (JsonParseException e) {
				throw new ConfigurationException("Could not parse configuration file "+url,e);
			} catch (JsonProcessingException e) {
				throw new ConfigurationException("Could not process configuration file "+url,e);
			} catch (IOException e) {
				throw new ConfigurationException("Could not load configuration file "+url,e);
			}
		} catch (IOException e) {
			throw new ConfigurationException("Could not open configuration file "+url, e);
		}
	}

	static <T> T convert(final Object source, final Type type) throws ConfigurationException {
		final ObjectMapper mapper = parsingMapper();
		final JavaType constructType = mapper.getTypeFactory().constructType(type);
		checkArgument(mapper.canDeserialize(constructType),"%s is not a valid configuration class",constructType.toCanonical());
		try {
			return mapper.convertValue(source,constructType);
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException("Could not load configuration data",e);
		}
	}

	public static <T extends Configuration> T load(final String value, final Class<? extends T> clazz) throws ConfigurationException {
		final Yaml yaml=new Yaml();
		try {
			final Object load=yaml.load(value);
			return convert(load,clazz);
		} catch (YAMLException e) {
			throw new ConfigurationException("Could not parse configuration data",e);
		}
	}

	public static Configuration load(final String value) throws ConfigurationException {
		return load(value,Configuration.class);
	}

	public static String save(final Configuration config) throws ConfigurationException {
		try {
			return writingMapper().writeValueAsString(config);
		} catch (JsonProcessingException e) {
			throw new ConfigurationException("Could not save configuration",e);
		}
	}

}
