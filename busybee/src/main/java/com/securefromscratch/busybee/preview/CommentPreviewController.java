package com.securefromscratch.busybee.preview;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CommentPreviewController {

    private final CommentPreviewGenerationService commentPreviewGenerationService;
    private final CommentPreviewDeletionService commentPreviewDeletionService;

    CommentPreviewController(
            CommentPreviewGenerationService commentPreviewGenerationService,
            CommentPreviewDeletionService commentPreviewDeletionService
    ) {
        this.commentPreviewGenerationService = commentPreviewGenerationService;
        this.commentPreviewDeletionService = commentPreviewDeletionService;
    }

    @PostMapping("/link-preview/comment")
    TaskLinkPreviewResponse generate(Authentication authentication, @RequestBody CommentPreviewRequest request) {
        return commentPreviewGenerationService.generate(request, authentication.getName());
    }

    @PostMapping("/link-preview/comment/delete")
    PreviewDeleteResponse delete(@RequestBody CommentPreviewRequest request) {
        return commentPreviewDeletionService.delete(request);
    }

    @ExceptionHandler(InvalidPreviewRequestException.class)
    ResponseEntity<Map<String, String>> invalidPreviewRequest(InvalidPreviewRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
