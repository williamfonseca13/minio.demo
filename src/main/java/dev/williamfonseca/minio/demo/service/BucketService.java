package dev.williamfonseca.minio.demo.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.messages.Bucket;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BucketService {

   private final MinioClient minioClient;

   public BucketService(MinioClient minioClient) {
      this.minioClient = minioClient;
   }

   public void createBucket(String bucketName) throws Exception {
      final var bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
      final var found = minioClient.bucketExists(bucketExistsArgs);
      if (!found) {
         final var makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
         minioClient.makeBucket(makeBucketArgs);
      }
   }

   public void deleteBucket(String bucketName) throws Exception {
      final var removeBucketArgs = RemoveBucketArgs.builder().bucket(bucketName).build();
      minioClient.removeBucket(removeBucketArgs);
   }

   public List<String> listAllBuckets() throws Exception {
      return minioClient.listBuckets().stream().map(Bucket::name).toList();
   }

   public boolean bucketExists(String bucketName) throws Exception {
      final var bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
      return minioClient.bucketExists(bucketExistsArgs);
   }
}

