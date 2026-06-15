package com.jmarfildev.rockalendar.common.storage;

import java.io.ByteArrayInputStream;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.StorageException;
import com.jmarfildev.rockalendar.config.properties.StorageProperties;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    public String upload(byte[] data, String key, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageProperties.bucket())
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(new ByteArrayInputStream(data), data.length));
            return storageProperties.publicUrlBase() + "/" + key;
        } catch (S3Exception e) {
            log.error("Error subiendo fichero al almacenamiento key={}: {}", key, e.getMessage());
            throw new StorageException(ErrorConstants.STORAGE_UPLOAD_FAILED, e);
        }
    }

    public String getPublicUrl(String key) {
        return storageProperties.publicUrlBase() + "/" + key;
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.bucket())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            log.warn("No se pudo eliminar el fichero del almacenamiento key={}: {}", key, e.getMessage());
        }
    }
}
