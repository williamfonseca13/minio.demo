package dev.williamfonseca.minio.demo.controller;

import dev.williamfonseca.minio.demo.service.BucketService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buckets")
public class BucketController {

   private static final Logger LOGGER = LoggerFactory.getLogger(BucketController.class);

   private final BucketService minioBucketService;

   public BucketController(BucketService minioBucketService) {
      this.minioBucketService = minioBucketService;
   }

   @Operation(summary = "Create a bucket")
   @PostMapping("/{bucketName}")
   public ResponseEntity<String> createBucket(@PathVariable String bucketName) {
      try {
         minioBucketService.createBucket(bucketName);
         return ResponseEntity.ok("Bucket created successfully.");
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body("Error creating bucket: " + e.getMessage());
      }
   }

   @Operation(summary = "Delete a bucket")
   @DeleteMapping("/{bucketName}")
   public ResponseEntity<String> deleteBucket(@PathVariable String bucketName) {
      try {
         minioBucketService.deleteBucket(bucketName);
         return ResponseEntity.ok("Bucket deleted successfully.");
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body("Error deleting bucket: " + e.getMessage());
      }
   }

   @Operation(summary = "List all buckets")
   @GetMapping
   public ResponseEntity<List<String>> listAllBuckets() {
      try {
         return ResponseEntity.ok(minioBucketService.listAllBuckets());
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(null);
      }
   }

   @Operation(summary = "Verify if a bucket exists")
   @GetMapping("/{bucketName}/exist")
   public ResponseEntity<Boolean> bucketExists(@PathVariable String bucketName) {
      try {
         return ResponseEntity.ok(minioBucketService.bucketExists(bucketName));
      } catch (Exception e) {
         LOGGER.error(e.getMessage(), e);
         return ResponseEntity.status(500).body(false);
      }
   }
}
