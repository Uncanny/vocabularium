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

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class Application {

	private final String name;
	private final Path repo;
	private final Path home;
	private final String version;
	private final String build;
	private final Optional<Integer> pid;
	private final ApplicationContext context;

	private Application(String name, Path home, Path repo, String version, String build, Integer pid, ApplicationContext context) {
		this.name = name;
		this.repo = repo;
		this.home = home;
		this.version = version;
		this.build = build;
		this.context = context;
		this.pid = Optional.ofNullable(pid);
	}

	public String name() {
		return this.name;
	}

	public Path repo() {
		return this.repo;
	}

	public Path home() {
		return this.home;
	}

	public String version() {
		return this.version;
	}

	public String build() {
		return this.build;
	}

	public Optional<Integer> pid() {
		return this.pid;
	}

	public ApplicationInstance instantiate(Path configFile) {
		final String id = this.context.instanceName(configFile);
		final Path lockFile=this.context.instanceLock(this.home,id);
		final Path instanceDir=this.context.instanceHome(this.home,id);
		System.setProperty("app.instance",instanceDir.toAbsolutePath().toString());
		return ApplicationInstance.create(id,instanceDir.normalize(),lockFile.normalize());
	}

	public void logContext(String... args) {
		ApplicationUtil.logContext(args);
	}

	public static Application forClass(final Class<?> appClass, ApplicationContext provider) {
		return
			new Application(
				getName(appClass),
				getPath("app.home"),
				getPath("app.repo"),
				getProperty("app.version"),
				getProperty("app.build"),
				getPid(),
				provider
			);
	}

	private static String getName(final Class<?> appClass) {
		final String name = System.getProperty("app.name");
		if(name==null) {
			return appClass.getName();
		}
		String ext=".bat";
		if(System.getenv("MSYSTEM")!=null) {
			ext=".sh";
		}
		return name+ext;
	}

	private static Path getPath(String property) {
		Path value=null;
		final String rawPath = System.getProperty(property);
		if(rawPath!=null) {
			try {
				value = Paths.get(rawPath);
			} catch (InvalidPathException e) {
				System.err.println("Invalid path for '"+property+"' ("+rawPath+")");
			}
		}
		return value;
	}

	private static String getProperty(String property) {
		return System.getProperty(property,"");
	}

	private static Integer getPid() {
		Integer pid=null;
		final String rawPid = System.getProperty("app.pid");
		if(rawPid!=null) {
			try {
				pid = Integer.parseInt(rawPid);
			} catch (NumberFormatException e) {
				System.err.println("Invalid PID");
			}
		}
		return pid;
	}

}
