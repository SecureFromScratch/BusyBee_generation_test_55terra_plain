package com.securefromscratch.busybee.ai;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CommentSummaryController {

    private final CommentSummaryService commentSummaryService;

    CommentSummaryController(CommentSummaryService commentSummaryService) {
        this.commentSummaryService = commentSummaryService;
    }

    @PostMapping("/ai/task/comment-summary")
    CommentSummaryResponse summarize(Authentication authentication, @RequestBody CommentSummaryRequest request) {
        return commentSummaryService.summarize(authentication, request, false);
    }

    @PostMapping("/ai/task/comment-summary/refresh")
    CommentSummaryResponse refresh(Authentication authentication, @RequestBody CommentSummaryRequest request) {
        return commentSummaryService.summarize(authentication, request, true);
    }

    @ExceptionHandler(InvalidCommentSummaryException.class)
    ResponseEntity<Map<String, String>> invalidSummary(InvalidCommentSummaryException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
