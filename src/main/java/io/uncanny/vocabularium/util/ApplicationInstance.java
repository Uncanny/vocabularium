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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileLock;
import java.nio.file.Path;

public final class ApplicationInstance implements AutoCloseable {

	private final String id;
	private final Path home;
	private final File file;
	private final FileOutputStream stream;
	private final FileLock lock;

	private ApplicationInstance(String id, Path home, File file, FileOutputStream stream, FileLock lock) {
		this.id = id;
		this.home = home;
		this.file = file;
		this.stream = stream;
		this.lock = lock;
		set("home",home.toAbsolutePath());
	}

	public String id() {
		return this.id;
	}

	public Path home() {
		return this.home;
	}

	public void set(String property, Object value) {
		final PrintWriter writer = new PrintWriter(this.stream);
		writer.printf("%s : %s%n",property,value);
		writer.flush();
		if(writer.checkError()) {
			System.err.printf("Could not set property %s with value %s%n",property,value);
		}
	}

	@Override
	public void close() {
		if(this.lock==null) {
			return; // Not locked
		}
		try {
			this.lock.release();
		} catch (IOException e) {
			System.err.printf("Could not release lock over instance lock file %s (%s)%n",this.file.getAbsolutePath(),e.getMessage());
		}
		closeQuietly(this.file,this.stream);
	}

	static ApplicationInstance create(String id, Path home, Path lockFile) {
		final File file = lockFile.toFile();
		file.getParentFile().mkdirs();
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new IllegalStateException("Could not create instance file "+file.getAbsolutePath());
		}
		FileOutputStream stream=null;
		try {
			stream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Could not open instance file "+file.getAbsolutePath());
		}
		try {
			FileLock lock=stream.getChannel().tryLock();
			if(lock!=null) {
				if(!home.toFile().mkdirs()) {
					if(!home.toFile().isDirectory()) {
						throw new IllegalStateException("Could not set-up instance "+id+" home directory ("+home+")");
					}
				}
				return new ApplicationInstance(id,home,file,stream,lock);
			} else {
				closeQuietly(file,stream);
				throw new IllegalStateException("Instance "+id+" is already locked ("+file.getAbsolutePath()+")");
			}
		} catch (IOException e) {
			closeQuietly(file, stream);
			throw new IllegalStateException("Could not lock instance file "+file.getAbsolutePath());
		}
	}

	private static void closeQuietly(File file, FileOutputStream stream) {
		try {
			stream.close();
		} catch (IOException e1) {
			System.err.printf("Could not close instance lock file %s (%s)%n",file.getAbsolutePath(),e1.getMessage());
		}
	}

}