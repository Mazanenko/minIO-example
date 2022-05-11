package com.example.minioexample.utill.impl;

import com.example.minioexample.utill.MinioUtil;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioUtilImpl implements MinioUtil {

    private final MinioClient minioClient;

    @Override
    public boolean isBucketExist(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("Error while trying check if bucket ({}) exists. Error msg: {}",
                    bucketName, e.getMessage());
        }
        return false;
    }

    @Override
    public void makeBucket(String bucketName) {
        if (bucketName == null || bucketName.isBlank()) {
            log.warn("Given bucket's name is null or empty");
            return;
        }
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("Error while trying make new bucket ({}). Error msg: {}",
                    bucketName, e.getMessage());
        }
    }

    @Override
    public void uploadObject(String bucketName, String objectName, String objectPath) {
        if (bucketName == null || bucketName.isBlank()) {
            log.warn("Given bucket's name is null or empty");
            return;
        }
        if (objectName == null || objectName.isBlank()) {
            log.warn("Given object's name is null or empty");
            return;
        }
        if (objectPath == null || objectPath.isBlank()) {
            log.warn("Given object's path is null or empty");
            return;
        }
        if (!isBucketExist(bucketName)) {
            makeBucket(bucketName);
        }
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(objectPath)
                            .build());
        } catch (Exception e) {
            log.error("Error while trying to upload object ({}) ({}). Error msg: {}",
                    objectName, objectPath, e.getMessage());
        }
    }

    public Object readObject(String bucketName, String objectName) {
        if (!isBucketExist(bucketName)) {
            throw new IllegalArgumentException("Given invalid bucket");
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalArgumentException("Given invalid objectName");
        }

        try (GetObjectResponse response =
                     minioClient.getObject(GetObjectArgs.builder()
                             .bucket(bucketName)
                             .object(objectName)
                             .build())) {

            byte[] bytes = response.readAllBytes();
            return SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            log.error("Error while trying to read object ({}) in bucket ({}). Error msg: {}",
                    objectName, bucketName, e.getMessage());
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public <T> ObjectWriteResponse uploadObjectAsStream(T object, String bucketName, String objectName) {
        if (object == null) {
//            log.warn("Given object is null");
            throw new IllegalArgumentException("Given object is null");
        }
        if (!isBucketExist(bucketName)) {
            makeBucket(bucketName);
        }

        byte[] data = SerializationUtils.serialize(object);
        if (data == null) {
            throw new SerializationFailedException(String.format("Cant serialize object for class %s"
                    , object.getClass()));
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, data.length, -1)
                    .contentType("application/octet-stream")
                    .build());

        } catch (Exception e) {
            log.error("Error while trying to upload object ({}) to bucket ({}). Error msg: {}",
                    objectName, bucketName, e.getMessage());
            throw new RuntimeException(e.getCause());
        }
    }
}

