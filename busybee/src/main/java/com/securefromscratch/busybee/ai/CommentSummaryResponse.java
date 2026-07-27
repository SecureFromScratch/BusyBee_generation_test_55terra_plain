package com.securefromscratch.busybee.ai;

import java.time.LocalDateTime;

public record CommentSummaryResponse(
        String summary,
        int summarizedCommentCount,
        int currentCommentCount,
        LocalDateTime summarizedLatestCommentAt,
        LocalDateTime currentLatestCommentAt,
        boolean stale,
        String generatedBy,
        String credentialSource,
        LocalDateTime generatedAt
) {
}
