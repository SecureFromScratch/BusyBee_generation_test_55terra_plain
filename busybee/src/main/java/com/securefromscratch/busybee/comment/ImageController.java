package com.securefromscratch.busybee.comment;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ImageController {

    private final ImageRetrievalService imageRetrievalService;

    ImageController(ImageRetrievalService imageRetrievalService) {
        this.imageRetrievalService = imageRetrievalService;
    }

    @GetMapping("/image")
    ResponseEntity<byte[]> image(@RequestParam("img") String imageId) {
        RetrievedImage image = imageRetrievalService.findImage(imageId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType())).body(image.content());
    }

    @ExceptionHandler(ImageStorageException.class)
    ResponseEntity<Map<String, String>> imageError(ImageStorageException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }
}
