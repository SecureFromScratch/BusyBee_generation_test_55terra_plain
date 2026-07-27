package com.securefromscratch.busybee.preview;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TaskPreviewUrlSupport {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?i)(https?://[^\\s]+|(?:www\\.)?[a-z0-9][a-z0-9.-]*\\.[a-z]{2,}(?:/[^\\s]*)?)"
    );
    private static final String TRAILING_URL_PUNCTUATION = ".,;:!?)]}";

    private TaskPreviewUrlSupport() {
    }

    static String validTaskId(String taskId) {
        return validId(taskId, "Task");
    }

    static String validCommentId(String commentId) {
        return validId(commentId, "Comment");
    }

    private static String validId(String id, String resourceName) {
        if (id == null) {
            throw new InvalidPreviewRequestException(resourceName + " ID is invalid.");
        }

        try {
            return UUID.fromString(id).toString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidPreviewRequestException(resourceName + " ID is invalid.");
        }
    }

    static boolean taskContainsUrl(String text, String requestedUrl) {
        return textContainsUrl(text, requestedUrl);
    }

    static boolean textContainsUrl(String text, String requestedUrl) {
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            if (normalizedUrl(matcher.group()).equals(normalizedUrl(requestedUrl))) {
                return true;
            }
        }
        return false;
    }

    static String normalizedUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPreviewRequestException("URL is invalid.");
        }

        String normalized = trimTrailingPunctuation(value.trim()).toLowerCase(Locale.ROOT);
        if (normalized.startsWith("https://")) {
            normalized = normalized.substring("https://".length());
        } else if (normalized.startsWith("http://")) {
            normalized = normalized.substring("http://".length());
        }
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring("www.".length());
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank() || !normalized.contains(".")) {
            throw new InvalidPreviewRequestException("URL is invalid.");
        }
        return normalized;
    }

    private static String trimTrailingPunctuation(String value) {
        int end = value.length();
        while (end > 0 && TRAILING_URL_PUNCTUATION.indexOf(value.charAt(end - 1)) >= 0) {
            end--;
        }
        return value.substring(0, end);
    }
}
