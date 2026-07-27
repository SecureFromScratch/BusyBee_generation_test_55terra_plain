package com.securefromscratch.busybee.task;

class UnknownTaskException extends RuntimeException {

    UnknownTaskException() {
        super("Task was not found.");
    }
}
