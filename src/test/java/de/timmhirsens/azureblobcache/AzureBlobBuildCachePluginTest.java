package de.timmhirsens.azureblobcache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.gradle.api.initialization.Settings;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.junit.jupiter.api.Test;

class AzureBlobBuildCachePluginTest {

	@Test
	void apply() {
		Settings settings = mock(Settings.class);
		BuildCacheConfiguration configuration = mock(BuildCacheConfiguration.class);
		when(settings.getBuildCache()).thenReturn(configuration);
		AzureBlobBuildCachePlugin plugin = new AzureBlobBuildCachePlugin();
		plugin.apply(settings);

		verify(configuration).registerBuildCacheService(AzureBlobStorageBuildCache.class, AzureBlobStorageBuildCacheServiceFactory.class);
	}
}
