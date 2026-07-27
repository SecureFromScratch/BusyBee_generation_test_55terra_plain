package com.securefromscratch.busybee.user;

import java.util.List;

public record UserProfileResponse(
        String username,
        boolean admin,
        List<String> entitlements,
        List<String> effectiveEntitlements,
        UserProfileSettings settings,
        AuthenticationEndpoints authentication
) {
    public record AuthenticationEndpoints(String loginUrl, String logoutUrl, String changePasswordUrl) {
    }
}
