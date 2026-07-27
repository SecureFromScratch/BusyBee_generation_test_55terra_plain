package com.securefromscratch.busybee.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserEntitlementService {

    private final UserEntitlementRepository userEntitlementRepository;

    UserEntitlementService(UserEntitlementRepository userEntitlementRepository) {
        this.userEntitlementRepository = userEntitlementRepository;
    }

    public List<String> entitlementsFor(String username) {
        return userEntitlementRepository.findEntitlements(username);
    }

    public List<String> findUsernames(String query) {
        return userEntitlementRepository.findUsernames(query == null ? "" : query.trim());
    }

    @Transactional
    public List<String> replaceEntitlements(String username, List<String> entitlements) {
        if (!userEntitlementRepository.userExists(username)) {
            throw new InvalidAdminRequestException("User was not found.");
        }
        List<String> normalized = normalizeEntitlements(entitlements);
        userEntitlementRepository.replaceEntitlements(username, normalized);
        return normalized;
    }

    private List<String> normalizeEntitlements(List<String> entitlements) {
        if (entitlements == null || !EntitlementCatalog.SET.containsAll(entitlements)) {
            throw new InvalidAdminRequestException("Entitlements are invalid.");
        }
        return entitlements.stream().distinct().sorted().toList();
    }
}
