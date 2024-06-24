package dev.williamfonseca.minio.demo.service;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
public class BucketServiceTest {

   @Container
   private final MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio:latest")
           .withUserName("admin")
           .withPassword("admin12345678")
           .withExposedPorts(9000);

   private MinioClient minioClient;

   @Autowired
   private BucketService bucketService;

   @BeforeEach
   void setUp() {

      minioClient = MinioClient.builder()
              .endpoint("http://%s:%d".formatted(MINIO_CONTAINER.getHost(), MINIO_CONTAINER.getFirstMappedPort()))
              .credentials(MINIO_CONTAINER.getUserName(), MINIO_CONTAINER.getPassword())
              .build();

      bucketService = new BucketService(minioClient);
   }

   @Test
   void testCreateBucketWhenBucketDoesNotExist() throws Exception {
      bucketService.createBucket("test-bucket");
      var exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build());
      assertTrue(exists);
   }

   @Test
   void testCreateBucketWhenBucketExists() throws Exception {
      bucketService.createBucket("test-bucket");
      bucketService.createBucket("test-bucket");
      // Verify bucket exists and was not recreated
      var exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build());
      assertTrue(exists);
   }

   @Test
   void testDeleteBucket() throws Exception {
      bucketService.createBucket("test-bucket");
      bucketService.deleteBucket("test-bucket");
      var exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build());
      assertFalse(exists);
   }

   @Test
   void testListAllBuckets() throws Exception {
      bucketService.createBucket("test-bucket");
      List<String> buckets = bucketService.listAllBuckets();
      assertEquals(1, buckets.size());
      assertEquals("test-bucket", buckets.getFirst());
   }

   @Test
   void testBucketExists() throws Exception {
      bucketService.createBucket("test-bucket");
      var exists = bucketService.bucketExists("test-bucket");
      assertTrue(exists);
   }

   @Test
   void testBucketDoesNotExist() throws Exception {
      var exists = bucketService.bucketExists("nonexistent-bucket");
      assertFalse(exists);
   }
}
