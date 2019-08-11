package de.timmhirsens.azureblobcache;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.ContainerClientBuilder;
import com.azure.storage.common.credentials.SharedKeyCredential;

public class AzureBlobStorageBuildCacheServiceFactory implements BuildCacheServiceFactory<AzureBlobStorageBuildCache> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobStorageBuildCacheServiceFactory.class);

	@Override
	public BuildCacheService createBuildCacheService(AzureBlobStorageBuildCache config, Describer describer) {
		SharedKeyCredential sharedKeyCredentials = null;
		String accountName = config.getAccountName();
		if (config.getAccountKey() != null) {
			sharedKeyCredentials = new SharedKeyCredential(accountName, config.getAccountKey());
		}
		ContainerClient containerClient = createContainerClient(sharedKeyCredentials, accountName, config.getContainer());
		return new AzureBlobStorageBuildCacheService(containerClient);
	}

	private ContainerClient createContainerClient(@Nullable SharedKeyCredential sharedKeyCredentials, String accountName, String container) {
		String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
		ContainerClientBuilder builder = new ContainerClientBuilder()
				.endpoint(endpoint)
				.containerName(container)
				.httpClient(buildHttpClientFor(endpoint));
		if (sharedKeyCredentials != null) {
			builder.credential(sharedKeyCredentials);
		}
		return builder
				.buildClient();
	}

	private HttpClient buildHttpClientFor(String endpoint) {
		List<Proxy> proxies = ProxySelector.getDefault().select(URI.create(endpoint));
		return proxies.stream()
				.filter(p -> p.type() == Proxy.Type.HTTP)
				.findFirst()
				.map(p -> HttpClient.createDefault().proxy(() -> {
					ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, (InetSocketAddress) p.address());
					LOGGER.debug("Using Proxy {}", proxyOptions.address());
					return proxyOptions;
				}))
				.orElse(HttpClient.createDefault());
	}
}
