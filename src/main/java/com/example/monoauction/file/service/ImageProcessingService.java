package com.example.monoauction.file.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ImageProcessingService {

    @Value("${app.upload.directory}")
    private String uploadDirectory;

    @Value("${app.upload.image.quality:0.85}")
    private float imageQuality;

    @Value("${app.upload.image.thumbnail.width:150}")
    private int thumbnailWidth;

    @Value("${app.upload.image.thumbnail.height:150}")
    private int thumbnailHeight;

    @Value("${app.upload.image.thumbnail.enabled:true}")
    private boolean thumbnailEnabled;

    @Value("${app.upload.image.medium.width:800}")
    private int mediumWidth;

    @Value("${app.upload.image.medium.height:600}")
    private int mediumHeight;

    @Value("${app.upload.image.medium.enabled:true}")
    private boolean mediumEnabled;

    @Value("${app.upload.image.large.width:1200}")
    private int largeWidth;

    @Value("${app.upload.image.large.height:900}")
    private int largeHeight;

    @Value("${app.upload.image.large.enabled:true}")
    private boolean largeEnabled;

    @Value("${app.upload.image.original.max-width:2048}")
    private int originalMaxWidth;

    @Value("${app.upload.image.original.max-height:2048}")
    private int originalMaxHeight;

    @Value("${app.upload.image.convert-to-webp:true}")
    private boolean convertToWebp;

    @Value("${app.upload.image.keep-original:false}")
    private boolean keepOriginal;


    public Map<String, String> processImage(String filename) throws IOException {
        Map<String, String> processedImages = new HashMap<>();

        Path originalPath = Paths.get(uploadDirectory).resolve(filename);
        BufferedImage originalImage = ImageIO.read(originalPath.toFile());

        if (originalImage == null) {
            throw new IOException("Failed to read original image");
        }

        String baseFilename = getFilenameWithoutExtension(filename);
        String targetExtension = convertToWebp ? "webp" : getFileExtension(filename);

        String originalFilename = processOriginalImage(
                originalImage, baseFilename, targetExtension);
        processedImages.put("original", originalFilename);

        if (thumbnailEnabled) {
            String thumbnailFilename = createThumbnail(
                    originalImage, baseFilename, targetExtension);
            processedImages.put("thumbnail", thumbnailFilename);
        }

        if (mediumEnabled) {
            String mediumFilename = createMediumImage(
                    originalImage, baseFilename, targetExtension);
            processedImages.put("medium", mediumFilename);
        }

        if (largeEnabled) {
            String largeFilename = createLargeImage(
                    originalImage, baseFilename, targetExtension);
            processedImages.put("large", largeFilename);
        }

        if (!keepOriginal && convertToWebp) {
            Files.deleteIfExists(originalPath);
            log.info("Deleted original file: {}", filename);
        }

        logCompressionStats(filename, processedImages);

        return processedImages;

    }

    private String processOriginalImage(BufferedImage image, String baseFilename,
                                        String extension) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage processedImage = image;

        if(width > originalMaxWidth || height > originalMaxHeight) {
            processedImage = Thumbnails.of(image)
                    .size(originalMaxWidth, originalMaxHeight)
                    .asBufferedImage();

            log.info("Resized original from {}x{} to {}x{}", width, height, processedImage.getWidth(), processedImage.getHeight());

        }
        String filename = baseFilename + "_original." + extension;
        saveImage(processedImage, filename, extension);
        return filename;
    }

    private String createThumbnail(BufferedImage image, String baseFilename,
                                   String extension) throws IOException {

        BufferedImage thumbnail = Thumbnails.of(image)
                .size(thumbnailWidth, thumbnailHeight)
                .keepAspectRatio(true)
                .asBufferedImage();

        String filename = baseFilename + "_thumb." + extension;
        saveImage(thumbnail, filename, extension);

        log.info("Created thumbnail: {}x{}", thumbnail.getWidth(), thumbnail.getHeight());

        return filename;
    }

    private String createMediumImage(BufferedImage image, String baseFilename,
                                     String extension) throws IOException {

        BufferedImage medium = Thumbnails.of(image)
                .size(mediumWidth, mediumHeight)
                .keepAspectRatio(true)
                .asBufferedImage();

        String filename = baseFilename + "_medium." + extension;
        saveImage(medium, filename, extension);

        log.info("Created medium: {}x{}", medium.getWidth(), medium.getHeight());

        return filename;
    }

    private String createLargeImage(BufferedImage image, String baseFilename,
                                    String extension) throws IOException {

        BufferedImage large = Thumbnails.of(image)
                .size(largeWidth, largeHeight)
                .keepAspectRatio(true)
                .asBufferedImage();

        String filename = baseFilename + "_large." + extension;
        saveImage(large, filename, extension);

        log.info("Created large: {}x{}", large.getWidth(), large.getHeight());

        return filename;
    }

    private void saveImage(BufferedImage image, String filename,
                           String extension) throws IOException {

        Path outputPath = Paths.get(uploadDirectory).resolve(filename);

        if ("webp".equalsIgnoreCase(extension)) {
            saveAsWebP(image, outputPath);
        } else {
            Thumbnails.of(image)
                    .size(image.getWidth(), image.getHeight())
                    .outputFormat(extension)
                    .outputQuality(imageQuality)
                    .toFile(outputPath.toFile());
        }
    }

    private void saveAsWebP(BufferedImage image, Path outputPath) throws IOException {
        Thumbnails.of(image)
                .size(image.getWidth(), image.getHeight())
                .outputFormat("jpg")
                .outputQuality(imageQuality)
                .toFile(outputPath.toFile());


    }

    private String getFilenameWithoutExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    private void logCompressionStats(String originalFilename,
                                     Map<String, String> processedImages) {
        try {
            Path originalPath = Paths.get(uploadDirectory).resolve(originalFilename);
            long originalSize = Files.exists(originalPath) ?
                    Files.size(originalPath) : 0;

            long totalProcessedSize = 0;
            for (String filename : processedImages.values()) {
                Path path = Paths.get(uploadDirectory).resolve(filename);
                if (Files.exists(path)) {
                    totalProcessedSize += Files.size(path);
                }
            }

            if (originalSize > 0) {
                double compressionRatio =
                        (1 - (double) totalProcessedSize / originalSize) * 100;

                log.info("Compression stats - Original: {} KB, Processed: {} KB, Saved: {:.2f}%",
                        originalSize / 1024, totalProcessedSize / 1024, compressionRatio);
            }

        } catch (IOException e) {
            log.warn("Failed to calculate compression stats", e);
        }
    }

    public Map<String, Integer> getImageDimensions(String filename) throws IOException {
        Path imagePath = Paths.get(uploadDirectory).resolve(filename);
        BufferedImage image = ImageIO.read(imagePath.toFile());

        Map<String, Integer> dimensions = new HashMap<>();
        if (image != null) {
            dimensions.put("width", image.getWidth());
            dimensions.put("height", image.getHeight());
        }

        return dimensions;
    }

    public void deleteAllVersions(String baseFilename) throws IOException {
        String filenameWithoutExt = getFilenameWithoutExtension(baseFilename);

        String[] suffixes = {"_original", "_thumb", "_medium", "_large"};
        String[] extensions = {"jpg", "jpeg", "png", "webp"};

        for (String suffix : suffixes) {
            for (String ext : extensions) {
                String filename = filenameWithoutExt + suffix + "." + ext;
                Path filePath = Paths.get(uploadDirectory).resolve(filename);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Deleted: {}", filename);
                }
            }
        }
    }
}
