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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.google.common.io.Resources;

import io.uncanny.vocabularium.config.ConfigurationException;
import io.uncanny.vocabularium.config.ConfigurationFactory;
import io.uncanny.vocabularium.config.PublisherConfig;
import io.uncanny.vocabularium.util.Application;
import io.uncanny.vocabularium.util.ApplicationBootstrapException;
import io.uncanny.vocabularium.util.ApplicationInstance;
import io.uncanny.vocabularium.util.ApplicationManager;
import io.undertow.Undertow;

public final class VocabularyPublisher {

	private static final int CANNOT_READ_CONFIGURATION_FILE                =  -6;
	private static final int INVALID_CONFIGURATION_FILE_PATH               =  -5;
	private static final int CANNOT_PROCESS_CONFIGURATION_FILE_STATUS_CODE =  -4;
	private static final int CANNOT_LOAD_CONFIGURATION_FILE_STATUS_CODE    =  -3;
	private static final int INVALID_ARGUMENT_STATUS_CODE                  =  -2;
	private static final int INVALID_ARGUMENT_COUNT_STATUS_CODE            =  -1;

	public static void main(final String... args) throws FileNotFoundException, IOException {
		final ApplicationManager manager=ApplicationManager.builder().build();
		final Application application=manager.application(VocabularyPublisher.class);
		System.out.printf("Vocabulary Publisher%s%n",serviceVersion(application));
		try {
			final Path configFile = getConfigurationFile(application, args);
			final PublisherConfig config = loadConfiguration(configFile);
			try(ApplicationInstance instance=application.instantiate(configFile)) {
				System.out.printf("- Instance: %s%n",instance.id());
				System.out.printf("- Home: %s%n",instance.home().toAbsolutePath());
				instance.set("base",config.getBase());
				final PublisherUndertowConfigurator configurator=new PublisherUndertowConfigurator(instance);
				final Undertow server=configurator.configure(config);
				try {
					server.start();
					awaitTerminationRequest();
					server.stop();
				} finally {
					System.out.println("Publisher terminated.");
				}
			}
		} catch (ApplicationBootstrapException e) {
			application.logContext(args);
			System.err.println(e.getMessage());
			Throwable t=e.getCause();
			while(t!=null) {
				System.err.println(" - "+t.getMessage());
				t=t.getCause();
			}
			System.exit(e.getStatusCode());
		}
	}

	private static PublisherConfig loadConfiguration(Path configFile) {
		final String rawConfiguration=readConfigurationFile(configFile);
		try {
			return ConfigurationFactory.load(rawConfiguration,PublisherConfig.class);
		} catch (ConfigurationException e) {
			throw new ApplicationBootstrapException(CANNOT_PROCESS_CONFIGURATION_FILE_STATUS_CODE,"Could not process configuration file",e);
		}
	}

	private static String readConfigurationFile(Path configFile) {
		if(!configFile.toFile().isFile()) {
			throw new ApplicationBootstrapException(INVALID_CONFIGURATION_FILE_PATH,"Path does not point to a configuration file");
		} else if(!configFile.toFile().canRead()) {
			throw new ApplicationBootstrapException(CANNOT_READ_CONFIGURATION_FILE,"Cannot read configuration file");
		}
		try {
			return Resources.toString(configFile.toUri().toURL(), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new ApplicationBootstrapException(CANNOT_LOAD_CONFIGURATION_FILE_STATUS_CODE,"Could not load configuration file",e);
		}
	}

	private static Path getConfigurationFile(final Application application, final String... args) {
		if(args.length!=1) {
			String failure=
				String.format("Invalid argument number: 1 argument required (%d)%n",args.length)+
				String.format("USAGE: %s <path-to-config-file>%n",application.name())+
				String.format("  <path-to-config-file> : Path where the Vocabulary Publisher configuration file is available.%n");
			throw new ApplicationBootstrapException(INVALID_ARGUMENT_COUNT_STATUS_CODE, failure);
		}

		try {
			return Paths.get(args[0]).normalize();
		} catch (final InvalidPathException e) {
			throw new ApplicationBootstrapException(INVALID_ARGUMENT_STATUS_CODE,"Invalid configuration path",e);
		}
	}

	private static String serviceVersion(final Application application) {
		final StringBuilder builder=new StringBuilder();
		if(!application.version().isEmpty()) {
			builder.append(" v").append(application.version());
			if(!application.build().isEmpty()) {
				builder.append("-b").append(application.build());
			}
		}
		return builder.toString();
	}

	private static void awaitTerminationRequest() {
		System.out.println("Hit <ENTER> to exit...");
		try(final Scanner scanner = new Scanner(System.in)) {
			String readString = scanner.nextLine();
			while(readString != null) {
				if (readString.isEmpty()) {
					break;
				}
				if (scanner.hasNextLine()) {
					readString = scanner.nextLine();
				} else {
					readString = null;
				}
			}
		}
		System.out.println("<ENTER> detected.");
	}

}
