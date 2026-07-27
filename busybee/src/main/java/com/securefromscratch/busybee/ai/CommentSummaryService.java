package com.securefromscratch.busybee.ai;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securefromscratch.busybee.settings.UserSettingsService;

@Service
class CommentSummaryService {

    private static final String GENERATED_SUMMARY = "AI summary of the comment thread.";

    private final CommentSummaryRepository commentSummaryRepository;
    private final UserSettingsService userSettingsService;
    private final AiCredentialStatusService aiCredentialStatusService;

    CommentSummaryService(
            CommentSummaryRepository commentSummaryRepository,
            UserSettingsService userSettingsService,
            AiCredentialStatusService aiCredentialStatusService
    ) {
        this.commentSummaryRepository = commentSummaryRepository;
        this.userSettingsService = userSettingsService;
        this.aiCredentialStatusService = aiCredentialStatusService;
    }

    @Transactional
    CommentSummaryResponse summarize(Authentication authentication, CommentSummaryRequest request, boolean force) {
        String taskId = validTaskId(request.taskid());
        if (!commentSummaryRepository.taskExists(taskId)) {
            throw new InvalidCommentSummaryException("Task was not found.");
        }

        CommentStatistics statistics = commentSummaryRepository.commentStatistics(taskId);
        int threshold = userSettingsService.getSettings(authentication.getName()).summaryThresholdComments();
        if (!force && statistics.count() < threshold) {
            return null;
        }

        AiCredentialStatusResponse credential = aiCredentialStatusService.statusFor(authentication);
        if ("UNAVAILABLE".equals(credential.selection())) {
            throw new InvalidCommentSummaryException("AI credential is unavailable.");
        }

        String credentialSource = "PERSONAL_KEY".equals(credential.selection()) ? "PERSONAL_KEY" : "SERVER_KEY";
        commentSummaryRepository.save(taskId, GENERATED_SUMMARY, statistics, authentication.getName(), credentialSource);
        return response(taskId).orElseThrow();
    }

    java.util.Optional<CommentSummaryResponse> response(String taskId) {
        return commentSummaryRepository.findSummary(taskId)
                .map(summary -> response(summary, commentSummaryRepository.commentStatistics(taskId)));
    }

    private CommentSummaryResponse response(StoredCommentSummary summary, CommentStatistics currentStatistics) {
        boolean stale = summary.summarizedCommentCount() != currentStatistics.count()
                || !java.util.Objects.equals(summary.summarizedLatestCommentAt(), currentStatistics.latestCommentAt());
        return new CommentSummaryResponse(
                summary.summary(),
                summary.summarizedCommentCount(),
                currentStatistics.count(),
                summary.summarizedLatestCommentAt(),
                currentStatistics.latestCommentAt(),
                stale,
                summary.generatedBy(),
                summary.credentialSource(),
                summary.generatedAt()
        );
    }

    private String validTaskId(String taskId) {
        if (taskId == null) {
            throw new InvalidCommentSummaryException("Task ID is invalid.");
        }
        try {
            return UUID.fromString(taskId).toString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidCommentSummaryException("Task ID is invalid.");
        }
    }
}
