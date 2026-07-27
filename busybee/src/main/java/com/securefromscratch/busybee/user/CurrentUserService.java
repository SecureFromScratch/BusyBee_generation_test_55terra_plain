package com.securefromscratch.busybee.user;

import java.util.List;
import java.util.Set;

import com.securefromscratch.busybee.admin.UserEntitlementService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
class CurrentUserService {

    private static final Set<String> SUPPORTED_ENTITLEMENTS = Set.of(
            "IMPORT_ENABLED",
            "EXPORT_ENABLED",
            "PAID_LEVEL_1",
            "AI_ENABLED",
            "OCR_ENABLED"
    );

    private final UserEntitlementService userEntitlementService;

    CurrentUserService(UserEntitlementService userEntitlementService) {
        this.userEntitlementService = userEntitlementService;
    }

    CurrentUserResponse currentUser(Authentication authentication) {
        List<String> databaseEntitlements = userEntitlementService.entitlementsFor(authentication.getName());
        List<String> authorityEntitlements = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(SUPPORTED_ENTITLEMENTS::contains)
                .sorted()
                .toList();
        List<String> entitlements = databaseEntitlements.isEmpty() ? authorityEntitlements : databaseEntitlements;
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        List<String> effectiveEntitlements = admin
                ? SUPPORTED_ENTITLEMENTS.stream().sorted().toList()
                : entitlements;

        return new CurrentUserResponse(authentication.getName(), admin, entitlements, effectiveEntitlements);
    }
}
