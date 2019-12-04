package de.timmhirsens.azureblobcache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AzureBlobStorageBuildCacheServiceTest {

  private AzureBlobStorageBuildCacheService service;
  private BlobContainerClient containerClient;

  @BeforeEach
  void setUp() {
    containerClient = mock(BlobContainerClient.class, RETURNS_DEEP_STUBS);
    service = new AzureBlobStorageBuildCacheService(containerClient);
  }

  @Test
  void load() throws IOException {
    BlockBlobClient blobClient = mock(BlockBlobClient.class, RETURNS_DEEP_STUBS);
    when(containerClient.getBlobClient("testhashcode").getBlockBlobClient()).thenReturn(blobClient);
    when(blobClient.getBlobUrl()).thenReturn("http://localhost/my/blob");
    when(blobClient.exists()).thenReturn(true);
    BuildCacheEntryReader cacheEntryReader = mock(BuildCacheEntryReader.class);
    boolean load = service.load(new TestBuildCacheKey(), cacheEntryReader);
    verify(cacheEntryReader).readFrom(any());
    assertThat(load).isTrue();
  }

  @Test
  void loadNotExistent() throws IOException {
    BlockBlobClient blobClient = mock(BlockBlobClient.class, RETURNS_DEEP_STUBS);
    when(containerClient.getBlobClient("testhashcode").getBlockBlobClient()).thenReturn(blobClient);
    when(blobClient.getBlobUrl()).thenReturn("http://localhost/my/blob");
    when(blobClient.exists()).thenReturn(false);
    BuildCacheEntryReader cacheEntryReader = mock(BuildCacheEntryReader.class);
    boolean load = service.load(new TestBuildCacheKey(), cacheEntryReader);
    verify(cacheEntryReader, times(0)).readFrom(any());
    assertThat(load).isFalse();
  }

  @Test
  void loadThrows() throws IOException {
    BlockBlobClient blobClient = mock(BlockBlobClient.class, RETURNS_DEEP_STUBS);
    when(containerClient.getBlobClient("testhashcode").getBlockBlobClient()).thenReturn(blobClient);
    when(blobClient.getBlobUrl()).thenReturn("http://localhost/my/blob");
    when(blobClient.exists()).thenReturn(true);
    doThrow(UncheckedIOException.class).when(blobClient).download(any());
    BuildCacheEntryReader cacheEntryReader = mock(BuildCacheEntryReader.class);
    assertThrows(
        BuildCacheException.class, () -> service.load(new TestBuildCacheKey(), cacheEntryReader));
  }

  @Test
  void store() throws IOException {
    BlockBlobClient blobClient = mock(BlockBlobClient.class, RETURNS_DEEP_STUBS);
    when(containerClient.getBlobClient("testhashcode").getBlockBlobClient()).thenReturn(blobClient);
    when(blobClient.getBlobUrl()).thenReturn("http://localhost/my/blob");
    BuildCacheEntryWriter cacheEntryWriter = mock(BuildCacheEntryWriter.class);
    service.store(new TestBuildCacheKey(), cacheEntryWriter);
    verify(cacheEntryWriter).writeTo(any());
    verify(blobClient).upload(any(), anyLong());
  }

  @Test
  void storeException() throws IOException {
    BlockBlobClient blobClient = mock(BlockBlobClient.class, RETURNS_DEEP_STUBS);
    when(containerClient.getBlobClient("testhashcode").getBlockBlobClient()).thenReturn(blobClient);
    when(blobClient.getBlobUrl()).thenReturn("http://localhost/my/blob");
    BuildCacheEntryWriter cacheEntryWriter = mock(BuildCacheEntryWriter.class);
    when(blobClient.upload(any(), anyLong())).thenThrow(UncheckedIOException.class);
    assertThrows(
        BuildCacheException.class, () -> service.store(new TestBuildCacheKey(), cacheEntryWriter));
  }

  @Test
  void close() {
    assertDoesNotThrow(() -> service.close());
  }

  static final class TestBuildCacheKey implements BuildCacheKey {

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
