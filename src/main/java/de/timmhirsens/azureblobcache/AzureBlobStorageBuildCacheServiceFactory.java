package de.timmhirsens.azureblobcache;

import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;

import com.azure.storage.common.credentials.SharedKeyCredential;

public class AzureBlobStorageBuildCacheServiceFactory implements BuildCacheServiceFactory<AzureBlobStorageBuildCache> {
	@Override
	public BuildCacheService createBuildCacheService(AzureBlobStorageBuildCache config, Describer describer) {
		SharedKeyCredential sharedKeyCredentials = new SharedKeyCredential(config.getAccountName(), config.getAccountKey());
		return new AzureBlobStorageBuildCacheService(sharedKeyCredentials, config.getContainer());
	}
}
