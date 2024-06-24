package dev.williamfonseca.minio.demo.controller;

import dev.williamfonseca.minio.demo.service.FileService;
import io.minio.StatObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

   private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

   private final FileService fileService;

   public FileController(FileService fileService) {
      this.fileService = fileService;
   }

   @Operation(summary = "Upload a file to a bucket")
   @PostMapping(value = "/{bucketName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public ResponseEntity<FileService.UploadDto> uploadFile(@RequestParam(value = "file") MultipartFile file, @PathVariable String bucketName) {
      try {
         final var objectWriteResponse = fileService.uploadFile(bucketName, file);
         return ResponseEntity.ok(objectWriteResponse);
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }

   @Operation(summary = "Delete a file from a bucket")
   @DeleteMapping("/{bucketName}/{filename}")
   public ResponseEntity<String> deleteFile(@PathVariable String bucketName, @PathVariable String filename) {
      try {
         fileService.deleteFile(bucketName, filename);
         return ResponseEntity.ok("File deleted successfully.");
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body("Error deleting file: " + e.getMessage());
      }
   }

   @Operation(summary = "List all files in a bucket")
   @GetMapping("/{bucketName}")
   public ResponseEntity<List<String>> getAllBucketFiles(@PathVariable String bucketName) {
      try {
         return ResponseEntity.ok(fileService.listBucketFiles(bucketName));
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }

   @Operation(summary = "Download a file from a bucket")
   @GetMapping("/{bucketName}/{filename}")
   public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String bucketName, @PathVariable String filename) {
      try {
         final var inputStream = fileService.downloadFile(bucketName, filename);
         return ResponseEntity.ok()
                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
                 .body(new InputStreamResource(inputStream));
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }

   public record URL(String url) {
   }

   @Operation(summary = "Create a file URL")
   @GetMapping("url/{bucketName}/{filename}")
   public ResponseEntity<URL> getFileUrl(@PathVariable String bucketName, @PathVariable String filename) {
      try {
         final var url = fileService.generateFileUrl(bucketName, filename);
         return ResponseEntity.ok().body(new URL(url));
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }

   @Operation(summary = "Create a file URL")
   @GetMapping("makePublic/{bucketName}/{filename}")
   public ResponseEntity<Boolean> makeFilePublic(@PathVariable String bucketName, @PathVariable String filename) {
      try {
         fileService.makeObjectPublic(bucketName, filename);
         return ResponseEntity.ok().body(true);
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(false);
      }
   }

   @Operation(summary = "Create a file URL")
   @GetMapping("stat/{bucketName}/{filename}")
   public ResponseEntity<StatObjectResponse> getStat(@PathVariable String bucketName, @PathVariable String filename) {
      try {
         return ResponseEntity.ok().body(fileService.getObjectPath(bucketName, filename));
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }
}
