package com.securefromscratch.busybee.ai;

public record AiCredentialStatusResponse(
        String provider,
        String model,
        String providerType,
        boolean personalCredentialConfigured,
        String personalCredentialSuffix,
        boolean serverCredentialAvailable,
        boolean serverCredentialAllowed,
        String selection
) {
}
