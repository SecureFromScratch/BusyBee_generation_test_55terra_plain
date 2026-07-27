package com.securefromscratch.busybee.comment;

class ImageStorageException extends RuntimeException {

    ImageStorageException(String message) {
        super(message);
    }

    ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
