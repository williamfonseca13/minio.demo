package dev.williamfonseca.minio.demo.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

   private final MinioClient minioClient;

   public FileService(MinioClient minioClient) {
      this.minioClient = minioClient;
   }

   public UploadDto uploadFile(String bucketName, MultipartFile file) throws Exception {
      try (final var inputStream = file.getInputStream()) {

         final var found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
         if (!found)
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

         final var encodedObjectName = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8);

         final var args = PutObjectArgs.builder()
                 .bucket(bucketName)
                 .object(encodedObjectName)
                 .stream(inputStream, inputStream.available(), -1)
                 .contentType(file.getContentType())
                 .build();

         final var objectWriteResponse = minioClient.putObject(args);
         return new UploadDto(
                 objectWriteResponse.versionId(),
                 objectWriteResponse.etag(),
                 objectWriteResponse.bucket()
         );
      }
   }

   public record UploadDto(String versionId, String etag, String bucket) {
   }

   public List<String> listBucketFiles(String bucketName) throws Exception {
      final var fileNames = new ArrayList<String>();
      final var listObjectsArgs = ListObjectsArgs.builder().bucket(bucketName).build();
      final var results = minioClient.listObjects(listObjectsArgs);
      for (var result : results)
         fileNames.add(result.get().objectName());
      return fileNames;
   }

   public void deleteFile(String bucketName, String oldFilename) throws Exception {
      final var args = RemoveObjectArgs.builder()
              .bucket(bucketName)
              .object(oldFilename)
              .build();
      minioClient.removeObject(args);
   }

   public InputStream downloadFile(String bucketName, String filename) throws Exception {
      final var args = GetObjectArgs.builder().bucket(bucketName)
              .object(filename)
              .build();
      return minioClient.getObject(args);
   }

   public String generateFileUrl(String bucketName, String filename) throws Exception {
      final var presignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(bucketName)
              .object(URLEncoder.encode(filename, StandardCharsets.UTF_8))
              .expiry(2, TimeUnit.HOURS)
              .build();
      return minioClient.getPresignedObjectUrl(presignedObjectUrlArgs);
   }

   public void makeObjectPublic(String bucketName, String objectName) {
      try {

         // Define the bucket policy for public read access
         String policy = "{\n" +
                         "  \"Version\": \"2012-10-17\",\n" +
                         "  \"Statement\": [\n" +
                         "    {\n" +
                         "      \"Effect\": \"Allow\",\n" +
                         "      \"Principal\": \"*\",\n" +
                         "      \"Action\": [\"s3:GetObject\"],\n" +
                         "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/" + objectName + "\"]\n" +
                         "    }\n" +
                         "  ]\n" +
                         "}";

         // Set the policy
         minioClient.setBucketPolicy(
                 SetBucketPolicyArgs.builder()
                         .bucket(bucketName)
                         .config(policy)
                         .build());
         System.out.println(this.generateFileUrl(bucketName, objectName));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public StatObjectResponse getObjectPath(String bucketName, String objectName) {
      try {
         // Get object stat to ensure object exists and retrieve its details
         final var stat = minioClient.statObject(
                 StatObjectArgs.builder().bucket(bucketName).object(objectName).build());

         // Construct the object path
         return stat;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }
}

