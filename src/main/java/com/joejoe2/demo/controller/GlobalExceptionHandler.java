package com.joejoe2.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<Map<String, String>> handBadException(Exception ex, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("info", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handValidateException(Exception ex, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("info", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handException(Exception ex, WebRequest request) {
        ex.printStackTrace();
        Map<String, String> body = new HashMap<>();
        body.put("info", "unknown error, please try again later !");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
