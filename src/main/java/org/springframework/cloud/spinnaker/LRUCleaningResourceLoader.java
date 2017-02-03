/*
 * Copyright 2017 Pivotal, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.spinnaker;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

/**
 * Handle cleaning our resources to ensure disk space
 */
public class LRUCleaningResourceLoader extends DelegatingResourceLoader {

	private static final Logger log = LoggerFactory.getLogger(LRUCleaningResourceLoader.class);

	private final File repositoryCache;

	private final ResourceLoader delegate;

	private final Map<File, Void> lruCache = new LRUCache();

	private final float targetFreeSpaceRatio;

	LRUCleaningResourceLoader(ResourceLoader delegate, float targetFreeSpaceRatio, File repositoryCache) {

		Assert.notNull(delegate, "delegate cannot be null");
		Assert.isTrue(0 < targetFreeSpaceRatio && targetFreeSpaceRatio < 1, "targetFreeSpaceRatio should be between 0 and 1");
		this.delegate = delegate;
		this.targetFreeSpaceRatio = targetFreeSpaceRatio;
		this.repositoryCache = repositoryCache;
	}



	@Override
	public Resource getResource(String location) {

		Resource resource = delegate.getResource(location);
		try {
			File file = resource.getFile();
			synchronized (lruCache) {
				lruCache.put(file, null);
			}
			return resource;
		} catch (IOException e) {
			throw new RuntimeException(getClass().getSimpleName() +
				" is meant to work with File resolvable Resources. Exception trying to resolve " + location, e);
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return delegate.getClassLoader();
	}

	private class LRUCache extends LinkedHashMap<File, Void> {

		LRUCache() {
			super(5, 0.75f, true);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<File, Void> eldest) {

			for (Iterator<File> it = keySet().iterator(); it.hasNext();) {
				File file = it.next();
				log.info("Looking at {}, {} / {} = {}% free space", file, file.getFreeSpace(), file.getTotalSpace(), 100f * file.getFreeSpace() / file.getTotalSpace());
				if (shouldDelete(file) && it.hasNext()) { // never delete the most recent entry
					cleanup(file);
					it.remove();
				}
			}
			return false; // We already did some cleanup, don't let superclass do its logic
		}

		private void cleanup(File file) {

			if (repositoryCache != null && file.getPath().startsWith(repositoryCache.getPath())) {
				boolean success = FileSystemUtils.deleteRecursively(file.getParentFile());
				log.debug("[{}] Deleting {} parent directory to regain free space {}", success ? "SUCCESS" : "FAILED", file);
			} else {
				boolean success = file.delete();
				log.debug("[{}] Deleting {} to regain free space", success ? "SUCCESS" : "FAILED", file);
			}
		}
	}

	private boolean shouldDelete(File file) {
		return ((float) file.getFreeSpace()) / file.getTotalSpace() < targetFreeSpaceRatio;
	}

}
