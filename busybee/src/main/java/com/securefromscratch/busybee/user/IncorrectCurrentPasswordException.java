package com.securefromscratch.busybee.user;

class IncorrectCurrentPasswordException extends RuntimeException {

    IncorrectCurrentPasswordException() {
        super("Current password is incorrect.");
    }
}
