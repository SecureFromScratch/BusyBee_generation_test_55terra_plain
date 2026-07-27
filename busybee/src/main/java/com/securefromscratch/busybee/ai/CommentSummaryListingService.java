package com.securefromscratch.busybee.ai;

import org.springframework.stereotype.Service;

@Service
public class CommentSummaryListingService {

    private final CommentSummaryService commentSummaryService;

    CommentSummaryListingService(CommentSummaryService commentSummaryService) {
        this.commentSummaryService = commentSummaryService;
    }

    public CommentSummaryResponse summaryFor(String taskId) {
        return commentSummaryService.response(taskId).orElse(null);
    }
}
