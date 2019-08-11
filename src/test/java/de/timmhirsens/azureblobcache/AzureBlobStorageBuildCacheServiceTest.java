package de.timmhirsens.azureblobcache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;

import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;

class AzureBlobStorageBuildCacheServiceTest {

	private AzureBlobStorageBuildCacheService service;
	private ContainerClient containerClient;

	@BeforeEach
	void setUp() {
		containerClient = mock(ContainerClient.class);
		service = new AzureBlobStorageBuildCacheService(containerClient);
	}

	@Test
	void load() throws IOException {
		BlockBlobClient blobClient = mock(BlockBlobClient.class);
		when(containerClient.getBlockBlobClient("testhashcode")).thenReturn(blobClient);
		URL blobUrl = URI.create("http://localhost/my/blob").toURL();
		when(blobClient.getBlobUrl()).thenReturn(blobUrl);
		when(blobClient.exists()).thenReturn(new SimpleResponse<>(new HttpRequest(HttpMethod.GET, blobUrl), 200, new HttpHeaders(), true));
		BuildCacheEntryReader cacheEntryReader = mock(BuildCacheEntryReader.class);
		boolean load = service.load(new TestBuildCacheKey(), cacheEntryReader);
		verify(cacheEntryReader).readFrom(any());
		assertThat(load).isTrue();
	}

	@Test
	void loadNotExistent() throws IOException {
		BlockBlobClient blobClient = mock(BlockBlobClient.class);
		when(containerClient.getBlockBlobClient("testhashcode")).thenReturn(blobClient);
		URL blobUrl = URI.create("http://localhost/my/blob").toURL();
		when(blobClient.getBlobUrl()).thenReturn(blobUrl);
		when(blobClient.exists()).thenReturn(new SimpleResponse<>(new HttpRequest(HttpMethod.GET, blobUrl), 200, new HttpHeaders(), false));
		BuildCacheEntryReader cacheEntryReader = mock(BuildCacheEntryReader.class);
		boolean load = service.load(new TestBuildCacheKey(), cacheEntryReader);
		verify(cacheEntryReader, times(0)).readFrom(any());
		assertThat(load).isFalse();
	}

	@Test
	void loadThrows() throws IOException {
		BlockBlobClient blobClient = mock(BlockBlobClient.class);
		when(containerClient.getBlockBlobClient("testhashcode")).thenReturn(blobClient);
		URL blobUrl = URI.create("http://localhost/my/blob").toURL();
		when(blobClient.getBlobUrl()).thenReturn(blobUrl);
		when(blobClient.exists()).thenReturn(new SimpleResponse<>(new HttpRequest(HttpMethod.GET, blobUrl), 200, new HttpHeaders(), true));
		when(blobClient.download(any())).thenThrow(UncheckedIOException.class);
		BuildCacheEntryReader cacheEntryReader = mock(BuildCacheEntryReader.class);
		assertThrows(BuildCacheException.class, () -> service.load(new TestBuildCacheKey(), cacheEntryReader));
	}

	@Test
	void store() throws IOException {
		BlockBlobClient blobClient = mock(BlockBlobClient.class);
		when(containerClient.getBlockBlobClient("testhashcode")).thenReturn(blobClient);
		URL blobUrl = URI.create("http://localhost/my/blob").toURL();
		when(blobClient.getBlobUrl()).thenReturn(blobUrl);
		BuildCacheEntryWriter cacheEntryWriter = mock(BuildCacheEntryWriter.class);
		service.store(new TestBuildCacheKey(), cacheEntryWriter);
		verify(cacheEntryWriter).writeTo(any());
		verify(blobClient).upload(any(), anyLong());
	}

	@Test
	void storeException() throws IOException {
		BlockBlobClient blobClient = mock(BlockBlobClient.class);
		when(containerClient.getBlockBlobClient("testhashcode")).thenReturn(blobClient);
		URL blobUrl = URI.create("http://localhost/my/blob").toURL();
		when(blobClient.getBlobUrl()).thenReturn(blobUrl);
		BuildCacheEntryWriter cacheEntryWriter = mock(BuildCacheEntryWriter.class);
		when(blobClient.upload(any(), anyLong())).thenThrow(IOException.class);
		assertThrows(BuildCacheException.class, () -> service.store(new TestBuildCacheKey(), cacheEntryWriter));
	}

	@Test
	void close() {
		assertDoesNotThrow(() -> service.close());
	}

	final static class TestBuildCacheKey implements BuildCacheKey {

		@Override
		public String getDisplayName() {
			return "testhashcode";
		}

		@Override
		public String getHashCode() {
			return "testhashcode";
		}

		@Override
		public byte[] toByteArray() {
			return new byte[0];
		}
	}

}
