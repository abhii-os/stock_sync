package com.stocksync.userservice.exception;

public class RecordAlreadyExistsException extends RuntimeException {

    public RecordAlreadyExistsException(String message) {
        super(message);
    }

    public RecordAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
