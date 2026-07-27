package com.securefromscratch.busybee.user;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.securefromscratch.busybee.ai.AiCredentialStatusService;
import com.securefromscratch.busybee.settings.UserSettingsResponse;
import com.securefromscratch.busybee.settings.UserSettingsService;

@Service
class UserProfileService {

    private final CurrentUserService currentUserService;
    private final UserSettingsService userSettingsService;
    private final AiCredentialStatusService aiCredentialStatusService;

    UserProfileService(
            CurrentUserService currentUserService,
            UserSettingsService userSettingsService,
            AiCredentialStatusService aiCredentialStatusService
    ) {
        this.currentUserService = currentUserService;
        this.userSettingsService = userSettingsService;
        this.aiCredentialStatusService = aiCredentialStatusService;
    }

    UserProfileResponse profile(Authentication authentication) {
        CurrentUserResponse user = currentUserService.currentUser(authentication);
        UserSettingsResponse storedSettings = userSettingsService.getSettings(user.username());
        UserProfileSettings settings = new UserProfileSettings(
                storedSettings.summaryThresholdComments(),
                aiCredentialStatusService.statusFor(authentication)
        );
        return new UserProfileResponse(
                user.username(),
                user.admin(),
                user.entitlements(),
                user.effectiveEntitlements(),
                settings,
                new UserProfileResponse.AuthenticationEndpoints("/login", "/logout", "/user/password")
        );
    }
}
