package com.securefromscratch.busybee.ai;

import java.time.LocalDateTime;

record StoredCommentSummary(
        String summary,
        int summarizedCommentCount,
        LocalDateTime summarizedLatestCommentAt,
        String generatedBy,
        String credentialSource,
        LocalDateTime generatedAt
) {
}
