package dev.williamfonseca.minio.demo.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

   @Value("${minio.url}")
   private String minioUrl;

   @Value("${minio.access-key}")
   private String minioAccessKey;

   @Value("${minio.secret-key}")
   private String minioSecretKey;

   @Value("${minio.port}")
   private int port;

   @Value("${minio.security}")
   private boolean secure;

   @Bean
   public MinioClient minioClient() {
      return MinioClient.builder()
              .endpoint(minioUrl, port, secure)
              .credentials(minioAccessKey, minioSecretKey)
              .build();
   }
}
