package com.securefromscratch.busybee.comment;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
class CommentCreationController {

    private final CommentCreationService commentCreationService;

    CommentCreationController(CommentCreationService commentCreationService) {
        this.commentCreationService = commentCreationService;
    }

    @PostMapping(path = "/comment", consumes = "multipart/form-data")
    CommentCreateResponse createComment(
            Authentication authentication,
            @RequestPart("commentFields") CommentCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return commentCreationService.createComment(authentication.getName(), request, imageUpload(file));
    }

    @ExceptionHandler(InvalidCommentException.class)
    ResponseEntity<Map<String, String>> invalidComment(InvalidCommentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }

    private ImageUpload imageUpload(MultipartFile file) {
        if (file == null) {
            return null;
        }

        try {
            return new ImageUpload(file.getOriginalFilename(), file.getBytes());
        } catch (IOException exception) {
            throw new InvalidCommentException("Image file could not be read.");
        }
    }
}
