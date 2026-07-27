package com.securefromscratch.busybee.settings;

import java.util.Map;

public record UserSettingsResponse(int summaryThresholdComments, Map<String, Object> aiCredential) {

    static UserSettingsResponse withoutAiCredential(int summaryThresholdComments) {
        return new UserSettingsResponse(summaryThresholdComments, Map.of());
    }
}
