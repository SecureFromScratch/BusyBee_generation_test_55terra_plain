package com.securefromscratch.busybee.session;

class InvalidSessionSlotException extends RuntimeException {

    InvalidSessionSlotException(String message) {
        super(message);
    }
}
