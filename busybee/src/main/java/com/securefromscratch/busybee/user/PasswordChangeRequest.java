package com.securefromscratch.busybee.user;

record PasswordChangeRequest(String currentPassword, String newPassword) {
}
