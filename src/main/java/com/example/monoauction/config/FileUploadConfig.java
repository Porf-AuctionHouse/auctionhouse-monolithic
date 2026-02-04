package com.example.monoauction.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.File;

@Configuration
public class FileUploadConfig {

    @Value("${app.upload.directory:uploads/items}")
    private String uploadDirectory;

    @PostConstruct
    public void init() {
        File directory = new File(uploadDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Upload directory created: " + uploadDirectory);
            } else {
                System.out.println("Failed to create upload directory: " + uploadDirectory);
            }
        }
    }


}
