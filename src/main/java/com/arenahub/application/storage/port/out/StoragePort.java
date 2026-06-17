package com.arenahub.application.storage.port.out;

public interface StoragePort {

    String upload(String key, byte[] content, String contentType);

    String getPublicUrl(String key);

    void delete(String key);
}
