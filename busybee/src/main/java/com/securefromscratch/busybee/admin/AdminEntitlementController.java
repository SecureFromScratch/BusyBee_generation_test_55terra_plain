package com.securefromscratch.busybee.admin;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AdminEntitlementController {

    private final AdminEntitlementService adminEntitlementService;

    AdminEntitlementController(AdminEntitlementService adminEntitlementService) {
        this.adminEntitlementService = adminEntitlementService;
    }

    @GetMapping("/admin/entitlements")
    EntitlementCatalogResponse catalog() {
        return new EntitlementCatalogResponse(adminEntitlementService.catalog());
    }

    @GetMapping("/admin/users")
    java.util.List<AdminUserResponse> users(@RequestParam(defaultValue = "") String query) {
        return adminEntitlementService.findUsers(query);
    }

    @PostMapping("/admin/user-entitlements")
    AdminUserResponse replaceEntitlements(@RequestBody ReplaceUserEntitlementsRequest request) {
        return adminEntitlementService.replaceEntitlements(request);
    }

    @ExceptionHandler(InvalidAdminRequestException.class)
    ResponseEntity<Map<String, String>> invalidRequest(InvalidAdminRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
