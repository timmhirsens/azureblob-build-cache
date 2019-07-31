package de.timmhirsens.azureblobcache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.apache.xerces.impl.dv.util.Base64;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory.Describer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AzureBlobStorageBuildCacheServiceFactoryTest {

	private Describer testDescriber;

	@BeforeEach
	void setUp() {
		testDescriber = new NoopBuildCacheDescriber();
	}

	@Test
	void createBuildCacheServiceNpe() {
		AzureBlobStorageBuildCacheServiceFactory serviceFactory = new AzureBlobStorageBuildCacheServiceFactory();
		assertThrows(NullPointerException.class, () -> serviceFactory.createBuildCacheService(new AzureBlobStorageBuildCache(), testDescriber));
	}

	@Test
	void createBuildCacheService() {
		AzureBlobStorageBuildCacheServiceFactory serviceFactory = new AzureBlobStorageBuildCacheServiceFactory();
		AzureBlobStorageBuildCache config = new AzureBlobStorageBuildCache();
		config.setAccountKey(Base64.encode("Test Account Key".getBytes(StandardCharsets.UTF_8)));
		config.setAccountName("MyAccount");
		config.setContainer("mycontainer");
		BuildCacheService cacheService = serviceFactory.createBuildCacheService(config, testDescriber);
		assertThat(cacheService).isNotNull();
	}

	private static class NoopBuildCacheDescriber implements Describer {

		@Override
		public Describer type(String type) {
			return this;
		}

		@Override
		public Describer config(String name, String value) {
			return this;
		}

	}
}
