package com.example.min_io_test.media;

import com.example.min_io_test.media.dto.MediaResponse;
import com.example.min_io_test.media.minio.MinioService;
import com.example.min_io_test.utils.MediaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MinioService minioService;

    @Value("${media.base-uri}")
    private String baseUri;

    @Override
    public MediaResponse uploadSingle(MultipartFile file, String folderName) {
        String newName = UUID.randomUUID().toString();
        String extension = MediaUtil.extractExtension(Objects.requireNonNull(file.getOriginalFilename()));
        String objectName = folderName + "/" + newName + "." + extension;

        try {
            minioService.uploadFile(file, objectName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return MediaResponse.builder()
                .name(newName + "." + extension)
                .contentType(file.getContentType())
                .extension(extension)
                .size(file.getSize())
                .uri(String.format("%s%s/%s", baseUri, folderName, newName + "." + extension))
                .build();
    }

    @Override
    public List<MediaResponse> uploadMultiple(List<MultipartFile> files, String folderName) {
        List<MediaResponse> mediaResponses = new ArrayList<>();
        files.forEach(file -> {
            MediaResponse mediaResponse = this.uploadSingle(file, folderName);
            mediaResponses.add(mediaResponse);
        });
        return mediaResponses;
    }

    @Override
    public MediaResponse loadMediaByName(String mediaName, String folderName) {
        String objectName = folderName + "/" + mediaName;
        try {
            String contentType = minioService.getFileContentType(objectName);
            return MediaResponse.builder()
                    .name(mediaName)
                    .contentType(contentType)
                    .extension(MediaUtil.extractExtension(mediaName))
                    .uri(String.format("%s%s/%s", baseUri, folderName, mediaName))
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public MediaResponse deleteMediaByName(String mediaName, String folderName) {
        String objectName = folderName + "/" + mediaName;
        try {
            minioService.deleteFile(objectName);
            return MediaResponse.builder()
                    .name(mediaName)
                    .extension(MediaUtil.extractExtension(mediaName))
                    .uri(String.format("%s%s/%s", baseUri, folderName, mediaName))
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Resource downloadMediaByName(String mediaName, String folderName) {
        String objectName = folderName + "/" + mediaName;
        try {
            InputStream inputStream = minioService.getFile(objectName);
            Path tempFile = Files.createTempFile("minio", mediaName);
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return new UrlResource(tempFile.toUri());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media has not been found!");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
