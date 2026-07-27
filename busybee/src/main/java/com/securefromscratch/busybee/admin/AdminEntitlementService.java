package com.securefromscratch.busybee.admin;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
class AdminEntitlementService {

    private final UserEntitlementService userEntitlementService;

    AdminEntitlementService(UserEntitlementService userEntitlementService) {
        this.userEntitlementService = userEntitlementService;
    }

    List<String> catalog() {
        return EntitlementCatalog.ALL;
    }

    List<AdminUserResponse> findUsers(String query) {
        return userEntitlementService.findUsernames(query).stream()
                .map(this::responseFor)
                .toList();
    }

    AdminUserResponse replaceEntitlements(ReplaceUserEntitlementsRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new InvalidAdminRequestException("Username is required.");
        }
        userEntitlementService.replaceEntitlements(request.username(), request.entitlements());
        return responseFor(request.username());
    }

    private AdminUserResponse responseFor(String username) {
        List<String> entitlements = userEntitlementService.entitlementsFor(username);
        return new AdminUserResponse(username, false, entitlements, entitlements);
    }
}
