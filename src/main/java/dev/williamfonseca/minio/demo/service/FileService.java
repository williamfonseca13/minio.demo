package dev.williamfonseca.minio.demo.service;

import io.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

         final var args = PutObjectArgs.builder()
                 .bucket(bucketName)
                 .object(file.getOriginalFilename())
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
}

