package de.timmhirsens.azureblobcache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.BuildCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.common.credentials.SharedKeyCredential;

public class AzureBlobStorageBuildCacheService implements BuildCacheService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobStorageBuildCacheService.class);

	private final SharedKeyCredential sharedKeyCredentials;
	private final String path;

	AzureBlobStorageBuildCacheService(SharedKeyCredential sharedKeyCredentials, String path) {
		this.sharedKeyCredentials = sharedKeyCredentials;
		this.path = path;
	}

	@Override
	public boolean load(BuildCacheKey buildCacheKey, BuildCacheEntryReader buildCacheEntryReader) throws BuildCacheException {
		try {
			ContainerClient containerClient = createContainerClient();
			BlockBlobClient blobClient = containerClient.getBlockBlobClient(buildCacheKey.getHashCode());
			LOGGER.debug("Downloading {}", blobClient.getBlobUrl().toExternalForm());
			Boolean exists = blobClient.exists().value();
			if (!exists) {
				LOGGER.debug("Cache Entry Blob not found at {}", blobClient.getBlobUrl().toExternalForm());
				return false;
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			blobClient.download(os);
			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			buildCacheEntryReader.readFrom(is);
			is.close();
			os.close();
			return true;
		} catch (Exception e) {
			throw new BuildCacheException(e.getMessage(), e);
		}
	}

	@Override
	public void store(BuildCacheKey buildCacheKey, BuildCacheEntryWriter buildCacheEntryWriter) throws BuildCacheException {
		try {
			ContainerClient containerClient = createContainerClient();
			BlockBlobClient blobClient = containerClient.getBlockBlobClient(buildCacheKey.getHashCode());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			buildCacheEntryWriter.writeTo(outputStream);
			byte[] data = outputStream.toByteArray();
			LOGGER.debug("Uploading {} bytes to {}", data.length, blobClient.getBlobUrl().toExternalForm());
			blobClient.upload(new ByteArrayInputStream(data), data.length);
			outputStream.close();
		} catch (IOException e) {
			throw new BuildCacheException(e.getMessage(), e);
		}
	}

	private ContainerClient createContainerClient() {
		String endpoint = String.format("https://%s.blob.core.windows.net", sharedKeyCredentials.accountName());
		return ContainerClient.containerClientBuilder().endpoint(endpoint).credential(sharedKeyCredentials).containerName(path)
				.httpClient(buildHttpClientFor(endpoint)).buildClient();
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

	@Override
	public void close() throws IOException {
		// no-op
	}

}
