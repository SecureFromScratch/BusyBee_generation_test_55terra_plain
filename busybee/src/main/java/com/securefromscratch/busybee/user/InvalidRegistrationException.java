package com.securefromscratch.busybee.user;

class InvalidRegistrationException extends RuntimeException {

    InvalidRegistrationException() {
        super("Username and password are required.");
    }
}
