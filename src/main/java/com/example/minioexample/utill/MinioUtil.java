package com.example.minioexample.utill;

import io.minio.ObjectWriteResponse;

public interface MinioUtil {
    boolean isBucketExist(String bucketName);

    void makeBucket(String bucketName);

    void uploadObject(String bucketName, String objectName, String objectPath);

    Object readObject(String bucketName, String objectName);

    <T> ObjectWriteResponse uploadObjectAsStream(T object, String bucketName, String objectName);


}
