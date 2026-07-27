package com.securefromscratch.busybee.user;

import com.securefromscratch.busybee.ai.AiCredentialStatusResponse;

public record UserProfileSettings(int summaryThresholdComments, AiCredentialStatusResponse aiCredential) {
}
