package de.timmhirsens.azureblobcache;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.gradle.caching.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureBlobStorageBuildCacheService implements BuildCacheService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AzureBlobStorageBuildCacheService.class);

  private final BlobContainerClient containerClient;

  AzureBlobStorageBuildCacheService(BlobContainerClient containerClient) {
    this.containerClient = containerClient;
  }

  @Override
  public boolean load(BuildCacheKey buildCacheKey, BuildCacheEntryReader buildCacheEntryReader)
      throws BuildCacheException {
    try {
      BlockBlobClient blobClient =
          containerClient.getBlobClient(buildCacheKey.getHashCode()).getBlockBlobClient();
      LOGGER.debug("Downloading {}", blobClient.getBlobUrl());
      Boolean exists = blobClient.exists();
      if (!exists) {
        LOGGER.debug("Cache Entry Blob not found at {}", blobClient.getBlobUrl());
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
  public void store(BuildCacheKey buildCacheKey, BuildCacheEntryWriter buildCacheEntryWriter)
      throws BuildCacheException {
    try {
      BlockBlobClient blobClient =
          containerClient.getBlobClient(buildCacheKey.getHashCode()).getBlockBlobClient();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      buildCacheEntryWriter.writeTo(outputStream);
      byte[] data = outputStream.toByteArray();
      LOGGER.debug("Uploading {} bytes to {}", data.length, blobClient.getBlobUrl());
      blobClient.upload(new ByteArrayInputStream(data), data.length);
      outputStream.close();
    } catch (UncheckedIOException | IOException e) {
      throw new BuildCacheException(e.getMessage(), e);
    }
  }

  @Override
  public void close() throws IOException {
    // no-op
  }
}
