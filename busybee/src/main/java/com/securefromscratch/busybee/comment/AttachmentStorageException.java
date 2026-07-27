package com.securefromscratch.busybee.comment;

class AttachmentStorageException extends RuntimeException {

    AttachmentStorageException(String message) {
        super(message);
    }

    AttachmentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
