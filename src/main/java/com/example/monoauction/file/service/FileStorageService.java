package com.example.monoauction.file.service;

import com.example.monoauction.common.execptions.BusinessException;
import com.example.monoauction.common.execptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.directory}")
    private String uploadDirectory;
    @Value("${app.upload.max-file-size}")
    private long maxFileSize;
    @Value("${app.upload.max-files-per-item}")
    private long maxFilesPerItem;
    @Value("${app.upload.allowed-extensions}")
    private List<String> allowedExtensions;
    @Value("${app.upload.base-url}")
    private String baseUrl;
    private final ImageProcessingService imageProcessingService;

    public Map<String, String> storeFile(MultipartFile file) throws IOException {

        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = generateUniqueFilename(originalFilename);

        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File uploaded successfully: {}", uniqueFilename);

        Map<String, String> processedImages = imageProcessingService.processImage(uniqueFilename);

        return processedImages;

    }

    public List<Map<String, String>> storeFilesWithProcessing(List<MultipartFile> files)
            throws IOException {

        if (files.size() > maxFilesPerItem) {
            throw new BusinessException(
                    "Maximum " + maxFilesPerItem + " files allowed per item");
        }

        List<Map<String, String>> allProcessedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            Map<String, String> processedImages = storeFile(file);
            allProcessedImages.add(processedImages);
        }

        return allProcessedImages;
    }

    public Resource loadFileAsResource(String filename) throws IOException {
        try{
            Path filePath = Paths.get(uploadDirectory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + filename);
            }

        } catch (Exception e) {
            throw new ResourceNotFoundException("File not found: " + filename);
        }
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDirectory).resolve(filename).normalize();
        Files.delete(filePath);
        log.info("File deleted successfully: {}", filename);
    }

    public void deleteFiles(List<String> fileNames) throws IOException {
        for (String filename : fileNames) {
            deleteFile(filename);
        }
    }

    private void validateFile(MultipartFile file){
        if(file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        if(file.getSize() > maxFileSize) {
            throw new BusinessException("File size exceeds maximum allowed size of " + (maxFileSize / 1048576) + " MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if(!allowedExtensions.contains(extension)) {
            throw new BusinessException("File type not allowed. Allowed types: " +
                    String.join(", ", allowedExtensions));
        }

        String contentType = file.getContentType();
        if(contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("File must be an image");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateUniqueFilename(String filename) {
        String name = filename.substring(0, filename.lastIndexOf(".")).replace(" ", "_");
        String extension = getFileExtension(filename);
        return UUID.randomUUID().toString() + "_" + name + extension;
    }

    public String getFileUrl(String filename) {
        return baseUrl + "/api/files/" + filename;
    }

    public List<String> getFileUrls(List<String> filenames) {
        return filenames.stream().map(this::getFileUrl).collect(Collectors.toList());
    }

    public String filenamesToUrls(String commaSeparatedFilenames) {
        if(commaSeparatedFilenames == null || commaSeparatedFilenames.isEmpty()) {
            return "";
        }

        String[] filenames = commaSeparatedFilenames.split(",");
        return Arrays.stream(filenames)
                .map(String::trim)
                .map(this::getFileUrl)
                .collect(Collectors.joining(","));
    }





}
