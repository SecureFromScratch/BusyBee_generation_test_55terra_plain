package com.securefromscratch.busybee.ocr;

class OcrException extends RuntimeException {

    OcrException(String message) {
        super(message);
    }

    OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}
