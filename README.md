# azureblob-build-cache

[![Apache License 2.0](https://img.shields.io/badge/License-Apache%20License%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://timmhirsens.visualstudio.com/azureblob-build-cache/_apis/build/status/fr1zle.azureblob-build-cache?branchName=master)](https://timmhirsens.visualstudio.com/azureblob-build-cache/_build/latest?definitionId=2&branchName=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fr1zle_azureblob-build-cache&metric=alert_status)](https://sonarcloud.io/dashboard?id=fr1zle_azureblob-build-cache)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=fr1zle_azureblob-build-cache&metric=coverage)](https://sonarcloud.io/dashboard?id=fr1zle_azureblob-build-cache)

This is a Gradle [build cache](https://docs.gradle.org/current/userguide/build_cache.html) implementation that uses the [Azure Blob Storage](https://azure.microsoft.com/services/storage/blobs/) as a storage backend for 
cache objects. It is inspired by the [S3 Build Cache Plugin](https://github.com/myniva/gradle-s3-build-cache).

## Usage

This plugin has not been used in production for any serious project. Feedback is very welcome. Feel free to open an [issue](https://github.com/fr1zle/azureblob-build-cache/issues/new) if you find a bug or have an idea for
improvement. Even better, open up a [PR](https://github.com/fr1zle/azureblob-build-cache/pulls) with the desired change!

### Applying the plugin

You can get the latest version from the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/de.timmhirsens.azureblobcache.caching). Add the following to your `settings.gradle` to apply the plugin:

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"        
        }
    }
    dependencies {
        classpath "de.timmhirsens.azureblobcache:azureblob-build-cache:0.0.1"
    }
}

apply plugin: 'de.timmhirsens.azureblobcache.caching'
```

### Configuration

The following configuration settings are required for using this plugin.

| Configuration Key | Description | Required? |
|-------------------|-------------|-----------|
| container | The name of the container in Azure Blob Storage where cache objects should be stored | **yes** |
| accountName | The account name to use for storing the objects | **yes** |
| accountKey | The account key for the account name (keep this secret) | **no** (the azure blob storage container has to be configured accordingly)|

An configuration may look like this:

```groovy
ext.isCiServer = System.getenv().containsKey("CI")
buildCache {
    local {
        enabled = !isCiServer
    }
    remote(de.timmhirsens.azureblobcache.AzureBlobStorageBuildCache) {
        container = 'cache'
        push = isCiServer
        accountName = "youraccountname"
        accountKey = "gsafdzgfizdgwizgewzgfzewugzewuz/sfwe3223442sdfs/fdsojfiodsuzfewfhewuig=="
    }
}
```

You can read more about Gradle Build Cache on the Grade [website](https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_configure)

### Expiring cache entries

This plugin does not expire cache entries. You can use the Blob Storage [Lifecycle Management](https://azure.microsoft.com/de-de/blog/azure-blob-storage-lifecycle-management-public-preview/) for this.

## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
