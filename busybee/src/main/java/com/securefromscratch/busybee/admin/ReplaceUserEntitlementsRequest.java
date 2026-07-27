package com.securefromscratch.busybee.admin;

import java.util.List;

record ReplaceUserEntitlementsRequest(String username, List<String> entitlements) {
}
