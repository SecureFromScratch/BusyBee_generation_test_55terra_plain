package com.securefromscratch.busybee.ai;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
class AiCredentialService {

    private final VaultPersonalCredentialStore vaultPersonalCredentialStore;
    private final AiCredentialStatusService aiCredentialStatusService;

    AiCredentialService(
            VaultPersonalCredentialStore vaultPersonalCredentialStore,
            AiCredentialStatusService aiCredentialStatusService
    ) {
        this.vaultPersonalCredentialStore = vaultPersonalCredentialStore;
        this.aiCredentialStatusService = aiCredentialStatusService;
    }

    AiCredentialStatusResponse get(Authentication authentication) {
        return aiCredentialStatusService.statusFor(authentication);
    }

    AiCredentialStatusResponse save(Authentication authentication, AiCredentialRequest request) {
        AiProvider provider = provider(request.provider());
        String apiKey = apiKey(request.apiKey());
        vaultPersonalCredentialStore.save(authentication.getName(), new StoredPersonalCredential(provider, apiKey));
        return aiCredentialStatusService.statusFor(authentication);
    }

    AiCredentialStatusResponse delete(Authentication authentication) {
        vaultPersonalCredentialStore.delete(authentication.getName());
        return aiCredentialStatusService.statusFor(authentication);
    }

    private AiProvider provider(String provider) {
        try {
            return AiProvider.valueOf(provider == null ? "" : provider);
        } catch (IllegalArgumentException exception) {
            throw new InvalidAiCredentialException("AI provider is invalid.");
        }
    }

    private String apiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new InvalidAiCredentialException("AI API key is required.");
        }
        return apiKey.trim();
    }
}
