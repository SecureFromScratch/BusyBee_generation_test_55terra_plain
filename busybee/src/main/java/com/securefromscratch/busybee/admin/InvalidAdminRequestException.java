package com.securefromscratch.busybee.admin;

class InvalidAdminRequestException extends RuntimeException {

    InvalidAdminRequestException(String message) {
        super(message);
    }
}
