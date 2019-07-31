package de.timmhirsens.azureblobcache;

import org.gradle.caching.configuration.AbstractBuildCache;

public class AzureBlobStorageBuildCache extends AbstractBuildCache {

	private String accountName;
	private String accountKey;
	private String container;

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(String accountKey) {
		this.accountKey = accountKey;
	}
}
