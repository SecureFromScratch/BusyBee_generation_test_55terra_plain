package com.securefromscratch.busybee.settings;

class InvalidSummaryThresholdException extends RuntimeException {

    InvalidSummaryThresholdException() {
        super("Summary threshold must be between 5 and 15.");
    }
}
