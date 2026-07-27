package com.securefromscratch.busybee.admin;

import java.util.List;

record AdminUserResponse(String username, boolean admin, List<String> entitlements, List<String> effectiveEntitlements) {
}
