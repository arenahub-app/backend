package com.arenahub.infrastructure.storage;

import com.arenahub.application.storage.port.out.StoragePort;
import com.arenahub.infrastructure.config.StorageProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class R2StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final StorageProperties props;

    public R2StorageAdapter(S3Client s3Client, StorageProperties props) {
        this.s3Client = s3Client;
        this.props = props;
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(props.bucket())
                        .key(key)
                        .contentType(contentType)
                        .contentLength((long) content.length)
                        .build(),
                RequestBody.fromBytes(content));
        return key;
    }

    @Override
    public String getPublicUrl(String key) {
        if (props.publicUrl() == null || props.publicUrl().isBlank()) return key;
        return props.publicUrl().stripTrailing() + "/" + key;
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build());
    }
}
