package com.securefromscratch.busybee.ai;

class AiCredentialStoreException extends RuntimeException {

    AiCredentialStoreException() {
        super("AI credential storage is unavailable.");
    }
}
