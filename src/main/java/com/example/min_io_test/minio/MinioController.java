package com.example.min_io_test.minio;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioService minioService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/",consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file) {
        try {
            String uuid = minioService.uploadFile(file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("File uploaded successfully\nFile name: " + uuid);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String objectName) {
        try {
            InputStream fileStream = minioService.getFile(objectName);
            InputStreamResource resource = new InputStreamResource(fileStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + objectName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
