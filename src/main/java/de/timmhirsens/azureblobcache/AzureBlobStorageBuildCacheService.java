package de.timmhirsens.azureblobcache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.BuildCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;

public class AzureBlobStorageBuildCacheService implements BuildCacheService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobStorageBuildCacheService.class);

	private final ContainerClient containerClient;

	AzureBlobStorageBuildCacheService(ContainerClient containerClient) {
		this.containerClient = containerClient;
	}

	@Override
	public boolean load(BuildCacheKey buildCacheKey, BuildCacheEntryReader buildCacheEntryReader) throws BuildCacheException {
		try {
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

	@Override
	public void close() throws IOException {
		// no-op
	}

}
