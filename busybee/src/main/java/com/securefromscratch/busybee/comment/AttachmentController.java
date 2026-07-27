package com.securefromscratch.busybee.comment;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AttachmentController {

    private final AttachmentRetrievalService attachmentRetrievalService;

    AttachmentController(AttachmentRetrievalService attachmentRetrievalService) {
        this.attachmentRetrievalService = attachmentRetrievalService;
    }

    @GetMapping("/attachment")
    ResponseEntity<byte[]> attachment(@RequestParam("file") String attachmentId) {
        RetrievedAttachment attachment = attachmentRetrievalService.findAttachment(attachmentId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.originalFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(attachment.content());
    }

    @ExceptionHandler(AttachmentStorageException.class)
    ResponseEntity<Map<String, String>> attachmentError(AttachmentStorageException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }
}
