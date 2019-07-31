package de.timmhirsens.azureblobcache;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.common.credentials.SharedKeyCredential;

public class AzureBlobStorageBuildCacheServiceFactory implements BuildCacheServiceFactory<AzureBlobStorageBuildCache> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobStorageBuildCacheServiceFactory.class);

	@Override
	public BuildCacheService createBuildCacheService(AzureBlobStorageBuildCache config, Describer describer) {
		SharedKeyCredential sharedKeyCredentials = new SharedKeyCredential(config.getAccountName(), config.getAccountKey());
		ContainerClient containerClient = createContainerClient(sharedKeyCredentials, config.getContainer());
		return new AzureBlobStorageBuildCacheService(containerClient);
	}

	private ContainerClient createContainerClient(SharedKeyCredential sharedKeyCredentials, String container) {
		String endpoint = String.format("https://%s.blob.core.windows.net", sharedKeyCredentials.accountName());
		return ContainerClient
				.containerClientBuilder()
				.endpoint(endpoint)
				.credential(sharedKeyCredentials)
				.containerName(container)
				.httpClient(buildHttpClientFor(endpoint))
				.buildClient();
	}

	private HttpClient buildHttpClientFor(String endpoint) {
		List<Proxy> proxies = ProxySelector.getDefault().select(URI.create(endpoint));
		proxies = proxies.stream().filter(p -> p.type() == Proxy.Type.HTTP).collect(Collectors.toList());
		if (!proxies.isEmpty()) {
			List<Proxy> finalProxies = proxies;
			return HttpClient.createDefault().proxy(() -> {
				ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, (InetSocketAddress) finalProxies.get(0).address());
				LOGGER.debug("Using Proxy {}", proxyOptions.address());
				return proxyOptions;
			});
		}
		return HttpClient.createDefault();
	}
}
