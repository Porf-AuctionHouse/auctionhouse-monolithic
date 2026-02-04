package com.example.monoauction.file.controller;

import com.example.monoauction.common.dto.ApiResponse;
import com.example.monoauction.file.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class FileUploadController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(@RequestParam("file")MultipartFile file) {
        try{
            String filename = fileStorageService.storeFile(file);
            String fileUrl = fileStorageService.getFileUrl(filename);

            Map<String, String> response = new HashMap<>();
            response.put("filename", filename);
            response.put("url", fileUrl);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));
            response.put("contentType", file.getContentType());

            return ResponseEntity.ok(new ApiResponse<>(true, "File uploaded successfully", response));
        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files) {
        try{
            List<String> fileNames = fileStorageService.storeFiles(files);
            List<String> fileUrls = fileStorageService.getFileUrls(fileNames);

            List<Map<String, String>> filesInfo = new ArrayList<>();

            for(int i = 0; i < files.size(); i++){
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("filename", fileNames.get(i));
                fileInfo.put("url", fileUrls.get(i));
                fileInfo.put("originalName", files.get(i).getOriginalFilename());
                fileInfo.put("size", String.valueOf(files.get(i).getSize()));
                fileInfo.put("contentType", files.get(i).getContentType());
                filesInfo.add(fileInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("files", filesInfo);
            response.put("count", files.size());

            return ResponseEntity.ok(
                    ApiResponse.success(files.size() + " files uploaded successfully", response)
            );


        } catch (IOException e) {
            log.error("Failed to upload multiple files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload multiple files: " + e.getMessage()));
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
