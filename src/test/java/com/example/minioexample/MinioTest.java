package com.example.minioexample;

import com.example.minioexample.entity.TestEntity;
import com.example.minioexample.utill.MinioUtil;
import io.minio.ObjectWriteResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MinioTest {

    private final MinioUtil minioUtil;

    @Autowired
    public MinioTest(MinioUtil minioUtil) {
        this.minioUtil = minioUtil;
    }

    @Test
    void isBucketExistShouldReturnTrue() {
        //Given
        String bucketName = "dims-bucket";

        //When
        boolean isExist = minioUtil.isBucketExist(bucketName);

        //Then
        Assertions.assertTrue(isExist);

    }

    @Test
    void uploadObjectAsStreamShouldUploadObject() {
        //Given
        TestEntity testEntity = new TestEntity(42L, "entityName", "some useful payload");
        String bucketName = "dims-bucket";
        String objName = "ObjName";

        //When
        ObjectWriteResponse response = minioUtil.uploadObjectAsStream(testEntity, bucketName, objName);

        //Then
        Assertions.assertEquals(objName, response.object());

    }

    @Test
    void readObjectShouldGetObjectFromMinIO() {
        //Given
        TestEntity testEntity = new TestEntity(42L, "entityName", "some useful payload");
        String bucketName = "dims-bucket";
        String objName = "ObjName";

        //When
        Object obj = minioUtil.readObject(bucketName, objName);

        //Then
        if (obj instanceof TestEntity entity) {
            Assertions.assertEquals(testEntity, entity);
        } else Assertions.fail("read obj not instance of TestEntity");
    }
}
