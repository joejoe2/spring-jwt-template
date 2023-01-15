package com.joejoe2.demo.exception;

public class ValidationError extends IllegalArgumentException {
    public ValidationError(String msg) {
        super(msg);
    }
}
