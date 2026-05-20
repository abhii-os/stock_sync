package com.stocksync.stock_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
    public class GlobalExceptionHandler {
        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleAllExceptions(Exception ex) {
            if (ex instanceof WebClientResponseException wcre) {
                 return new ResponseEntity<>(
                        "External Service Error: " + wcre.getMessage() + " - " + wcre.getResponseBodyAsString(),
                        wcre.getStatusCode()
                );
            }
            return new ResponseEntity<>("Internal Server Error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler({TransactionNotFoundException.class})
        public ResponseEntity<String> handleNotFound(RuntimeException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

    }