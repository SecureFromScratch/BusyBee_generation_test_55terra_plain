package com.securefromscratch.busybee.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AiCredentialStatusService {

    private final String geminiApiKey;
    private final String openAiApiKey;
    private final VaultPersonalCredentialStore vaultPersonalCredentialStore;

    public AiCredentialStatusService(
            @Value("${busybee.ai.gemini.api-key:}") String geminiApiKey,
            @Value("${OPENAI_API_KEY:}") String openAiApiKey,
            VaultPersonalCredentialStore vaultPersonalCredentialStore
    ) {
        this.geminiApiKey = geminiApiKey;
        this.openAiApiKey = openAiApiKey;
        this.vaultPersonalCredentialStore = vaultPersonalCredentialStore;
    }

    public AiCredentialStatusResponse statusFor(Authentication authentication) {
        AiProvider serverProvider = configuredServerProvider();
        boolean serverCredentialAvailable = serverProvider != null;
        boolean serverCredentialAllowed = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("AI_ENABLED"));
        StoredPersonalCredential personalCredential = vaultPersonalCredentialStore.find(authentication.getName())
                .orElse(null);
        boolean personalCredentialConfigured = personalCredential != null;
        AiProvider displayedProvider = personalCredentialConfigured
                ? personalCredential.provider()
                : serverProvider == null ? AiProvider.GEMINI_FLASH : serverProvider;
        String selection = personalCredentialConfigured
                ? "PERSONAL_KEY"
                : serverCredentialAvailable && serverCredentialAllowed ? "SERVER_KEY" : "UNAVAILABLE";

        return new AiCredentialStatusResponse(
                displayedProvider.provider(),
                displayedProvider.model(),
                displayedProvider.name(),
                personalCredentialConfigured,
                personalCredentialConfigured ? suffix(personalCredential.apiKey()) : "",
                serverCredentialAvailable,
                serverCredentialAllowed,
                selection
        );
    }

    private AiProvider configuredServerProvider() {
        if (!geminiApiKey.isBlank()) {
            return AiProvider.GEMINI_FLASH;
        }
        if (!openAiApiKey.isBlank()) {
            return AiProvider.GPT_5_NANO;
        }
        return null;
    }

    private String suffix(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 4));
    }
}
