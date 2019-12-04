package de.timmhirsens.azureblobcache;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Objects;

public class AzureBlobStorageBuildCacheServiceFactory
    implements BuildCacheServiceFactory<AzureBlobStorageBuildCache> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AzureBlobStorageBuildCacheServiceFactory.class);

  @Override
  public BuildCacheService createBuildCacheService(
      AzureBlobStorageBuildCache config, Describer describer) {
    StorageSharedKeyCredential sharedKeyCredentials = null;
    Objects.requireNonNull(config.getAccountName(), "Account Name is required");
    String accountName = config.getAccountName();
    if (config.getAccountKey() != null) {
      sharedKeyCredentials = new StorageSharedKeyCredential(accountName, config.getAccountKey());
    }
    BlobContainerClient containerClient =
        createContainerClient(sharedKeyCredentials, accountName, config.getContainer());
    return new AzureBlobStorageBuildCacheService(containerClient);
  }

  private BlobContainerClient createContainerClient(
      @Nullable StorageSharedKeyCredential sharedKeyCredentials,
      String accountName,
      String container) {
    String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
    BlobContainerClientBuilder builder =
        new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .containerName(container)
            .httpClient(buildHttpClientFor(endpoint));
    if (sharedKeyCredentials != null) {
      builder.credential(sharedKeyCredentials);
    }
    return builder.buildClient();
  }

  private HttpClient buildHttpClientFor(String endpoint) {
    List<Proxy> proxies = ProxySelector.getDefault().select(URI.create(endpoint));
    return proxies.stream()
        .filter(p -> p.type() == Proxy.Type.HTTP)
        .findFirst()
        .map(
            p ->
                new NettyAsyncHttpClientBuilder()
                    .proxy(
                        new ProxyOptions(ProxyOptions.Type.HTTP, (InetSocketAddress) p.address()))
                    .build())
        .orElse(HttpClient.createDefault());
  }
}
