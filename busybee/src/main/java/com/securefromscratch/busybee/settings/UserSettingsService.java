package com.securefromscratch.busybee.settings;

import org.springframework.stereotype.Service;

@Service
public class UserSettingsService {

    private static final int DEFAULT_SUMMARY_THRESHOLD = 5;
    private static final int MINIMUM_SUMMARY_THRESHOLD = 5;
    private static final int MAXIMUM_SUMMARY_THRESHOLD = 15;

    private final UserSettingsRepository userSettingsRepository;

    UserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    public UserSettingsResponse getSettings(String username) {
        int threshold = userSettingsRepository.findSummaryThreshold(username)
                .orElse(DEFAULT_SUMMARY_THRESHOLD);
        return UserSettingsResponse.withoutAiCredential(threshold);
    }

    UserSettingsResponse replaceSettings(String username, UserSettingsRequest request) {
        Integer threshold = request.summaryThresholdComments();
        if (threshold == null || threshold < MINIMUM_SUMMARY_THRESHOLD || threshold > MAXIMUM_SUMMARY_THRESHOLD) {
            throw new InvalidSummaryThresholdException();
        }

        userSettingsRepository.saveSummaryThreshold(username, threshold);
        return UserSettingsResponse.withoutAiCredential(threshold);
    }
}
