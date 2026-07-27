package com.securefromscratch.busybee.user;

import java.util.List;

public record CurrentUserResponse(
        String username,
        boolean admin,
        List<String> entitlements,
        List<String> effectiveEntitlements
) {
}
