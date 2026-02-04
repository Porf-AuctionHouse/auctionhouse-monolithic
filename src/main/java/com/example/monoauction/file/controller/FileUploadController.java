package com.example.monoauction.file.controller;

import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.file.service.FileStorageService;
import com.example.monoauction.file.service.ImageProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class FileUploadController {
    private final FileStorageService fileStorageService;
    private final ImageProcessingService imageProcessingService;

    @Value("${app.upload.directory}")
    private String uploadDirectory;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {
            Map<String, String> processedImages = fileStorageService.storeFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());

            Map<String, String> imageUrls = new HashMap<>();
            for (Map.Entry<String, String> entry : processedImages.entrySet()) {
                String sizeType = entry.getKey();
                String filename = entry.getValue();
                imageUrls.put(sizeType, fileStorageService.getFileUrl(filename));
            }
            response.put("images", imageUrls);

            String mainFilename = processedImages.get("original");
            response.put("filename", mainFilename);
            response.put("url", fileStorageService.getFileUrl(mainFilename));

            return ResponseEntity.ok(
                    ApiResponse.success("File uploaded and processed successfully", response));

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files) {

        try {
            List<Map<String, String>> allProcessedImages =
                    fileStorageService.storeFilesWithProcessing(files);

            List<Map<String, Object>> filesInfo = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                Map<String, String> processedImages = allProcessedImages.get(i);

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("originalName", files.get(i).getOriginalFilename());
                fileInfo.put("size", files.get(i).getSize());

                Map<String, String> imageUrls = new HashMap<>();
                for (Map.Entry<String, String> entry : processedImages.entrySet()) {
                    String sizeType = entry.getKey();
                    String filename = entry.getValue();
                    imageUrls.put(sizeType, fileStorageService.getFileUrl(filename));
                }
                fileInfo.put("images", imageUrls);

                String mainFilename = processedImages.get("original");
                fileInfo.put("filename", mainFilename);
                fileInfo.put("url", fileStorageService.getFileUrl(mainFilename));

                filesInfo.add(fileInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("files", filesInfo);
            response.put("count", files.size());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            files.size() + " files uploaded and processed successfully",
                            response));

        } catch (IOException e) {
            log.error("Failed to upload files", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload files: " + e.getMessage()));
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request){
        try {
            Resource resource = fileStorageService.loadFileAsResource(filename);

            String contentType = null;
            try {
                contentType = request.getServletContext()
                        .getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException e) {
                log.error("Could not determine file type");
            }

            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Failed to load file", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/{filename:.+}/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImageInfo(
            @PathVariable String filename) {

        try {
            Map<String, Integer> dimensions =
                    imageProcessingService.getImageDimensions(filename);

            Path filePath = Paths.get(uploadDirectory).resolve(filename);
            long fileSize = Files.size(filePath);

            Map<String, Object> info = new HashMap<>();
            info.put("filename", filename);
            info.put("width", dimensions.get("width"));
            info.put("height", dimensions.get("height"));
            info.put("size", fileSize);
            info.put("sizeKB", fileSize / 1024);
            info.put("url", fileStorageService.getFileUrl(filename));

            return ResponseEntity.ok(ApiResponse.success(info));

        } catch (IOException e) {
            log.error("Failed to get image info", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Image not found"));
        }
    }


    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String filename){
        try{
            fileStorageService.deleteFile(filename);
            return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));

        } catch (IOException e){
           log.error("Failed to delete file", e);
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(ApiResponse.error("Failed to delete file: " + e.getMessage()));
        }
    }

}
