package dev.williamfonseca.minio.demo.controller;

import dev.williamfonseca.minio.demo.service.FileService;
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
@RequestMapping("/file")
public class FileController {

   private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

   private final FileService fileService;

   public FileController(FileService fileService) {
      this.fileService = fileService;
   }

   @PostMapping(value = "/upload/{bucketName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public ResponseEntity<FileService.UploadDto> uploadFile(@RequestParam(value = "file") MultipartFile file, @PathVariable String bucketName) {
      try {
         final var objectWriteResponse = fileService.uploadFile(bucketName, file);
         return ResponseEntity.ok(objectWriteResponse);
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }

   @DeleteMapping("/delete/{bucketName}/{filename}")
   public ResponseEntity<String> deleteFile(@PathVariable String bucketName, @PathVariable String filename) {
      try {
         fileService.deleteFile(bucketName, filename);
         return ResponseEntity.ok("File deleted successfully.");
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body("Error deleting file: " + e.getMessage());
      }
   }

   @GetMapping("/files/{bucketName}")
   public ResponseEntity<List<String>> getAllBucketFiles(@PathVariable String bucketName) {
      try {
         return ResponseEntity.ok(fileService.listBucketFiles(bucketName));
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }

   @GetMapping("/download/{bucketName}/{filename}")
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
}
