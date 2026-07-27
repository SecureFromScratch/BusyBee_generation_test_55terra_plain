package com.securefromscratch.busybee.user;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException() {
        super("Username is already registered.");
    }
}
