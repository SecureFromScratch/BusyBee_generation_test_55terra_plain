package com.securefromscratch.busybee.comment;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
class ImageRetrievalService {

    private final ImageMetadataRepository imageMetadataRepository;
    private final ImageStorageService imageStorageService;

    ImageRetrievalService(ImageMetadataRepository imageMetadataRepository, ImageStorageService imageStorageService) {
        this.imageMetadataRepository = imageMetadataRepository;
        this.imageStorageService = imageStorageService;
    }

    RetrievedImage findImage(String imageId) {
        String validImageId = validImageId(imageId);
        StoredImage image = imageMetadataRepository.findImage(validImageId)
                .orElseThrow(() -> new ImageStorageException("Image was not found."));
        return new RetrievedImage(image.contentType(), imageStorageService.load(image));
    }

    private String validImageId(String imageId) {
        if (imageId == null) {
            throw new ImageStorageException("Image was not found.");
        }

        try {
            return UUID.fromString(imageId).toString();
        } catch (IllegalArgumentException exception) {
            throw new ImageStorageException("Image was not found.");
        }
    }
}
