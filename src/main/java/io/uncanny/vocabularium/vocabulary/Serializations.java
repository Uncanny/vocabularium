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
package io.uncanny.vocabularium.vocabulary;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import io.uncanny.vocabularium.vocabulary.Module.Format;

public final class Serializations {

	private Serializations() {
	}

	public static List<Path> generate(final Module module, final Path where) throws IOException {
		final SerializationManager manager = SerializationManager.create(module, where);
		final Builder<Path> builder=ImmutableList.builder();
		collectModuleSerializations(builder,manager,module);
		return builder.build();
	}

	public static List<Path> generate(final Catalog catalog, final Path where) throws IOException {
		final SerializationManager manager = SerializationManager.create(catalog, where);
		final Builder<Path> builder=ImmutableList.builder();
		for(final String moduleId:catalog.modules()) {
			collectModuleSerializations(builder,manager,catalog.get(moduleId));
		}
		return builder.build();
	}

	private static void collectModuleSerializations(final Builder<Path> builder, final SerializationManager manager, final Module module) {
		for(final Format format:Format.values()) {
			builder.add(manager.getSerialization(module, format));
		}
	}

}
