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
package io.uncanny.vocabularium.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public final class ApplicationManager {

	public static final class Builder {

		private String locksPath;
		private String instancesPath;
		private InstanceIdentifierProvider provider;

		private Builder() {
		}

		public Builder locksPath(String path) {
			this.locksPath = path;
			return this;
		}

		public Builder instancesPath(String path) {
			this.instancesPath = path;
			return this;
		}

		public Builder identifyWith(ApplicationManager.InstanceIdentifierProvider provider) {
			this.provider=provider;
			return this;
		}

		public ApplicationManager build() {
			return
				new ApplicationManager(
					this.locksPath==null?
						".locks":
						this.locksPath,
					this.instancesPath==null?
						".instances":
						this.instancesPath,
					this.provider==null?
						new DefaultInstanceIdentifierProvider():
						this.provider);
		}

	}

	@FunctionalInterface
	public interface InstanceIdentifierProvider {

		String identify(Path configFile);

	}

	private static final class DefaultInstanceIdentifierProvider implements ApplicationManager.InstanceIdentifierProvider {

		@Override
		public String identify(Path configFile) {
			return
				String.format(
					"I%08X",
					configFile.toFile().getAbsolutePath().
						replace(":","_").
						replace(System.lineSeparator(),"_").
						toLowerCase(Locale.ENGLISH).hashCode());
		}

	}

	private final String locksPath;
	private final String instancesPath;
	private final InstanceIdentifierProvider provider;

	private ApplicationManager(String locksPath, String instancesPath, ApplicationManager.InstanceIdentifierProvider provider) {
		this.locksPath = locksPath;
		this.instancesPath = instancesPath;
		this.provider = provider;
	}

	public Application application(Class<?> appClass) {
		return
			Application.
				forClass(
					appClass,
					new ApplicationContext(){
						@Override
						public String instanceName(Path configFilePath) {
							return provider.identify(configFilePath);
						}
						@Override
						public Path instanceLock(Path home, String id) {
							final Path relativeLockFile = Paths.get(locksPath,id);
							return home.resolve(relativeLockFile);
						}
						@Override
						public Path instanceHome(Path home, String id) {
							final Path relativeInstanceDir = Paths.get(instancesPath,id);
							return home.resolve(relativeInstanceDir);
						}
					}
				);
	}

	public static Builder builder() {
		return new Builder();
	}

}