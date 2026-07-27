package com.securefromscratch.busybee.user;

class InvalidNewPasswordException extends RuntimeException {

    InvalidNewPasswordException() {
        super("New password is required.");
    }
}
