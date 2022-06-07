package com.joejoe2.demo.controller;

import com.joejoe2.demo.data.InvalidRequestResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, TreeSet<String>> errors =new HashMap<>();
        for (FieldError error:ex.getFieldErrors()){
            TreeSet<String> messages = errors.getOrDefault(error.getField(), new TreeSet<>());
            messages.add(error.getDefaultMessage());
            errors.put(error.getField(), messages);
        }
        return ResponseEntity.badRequest().body(new InvalidRequestResponse(errors));
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, TreeSet<String>> errors =new HashMap<>();
        for (FieldError error:ex.getFieldErrors()){
            TreeSet<String> messages = errors.getOrDefault(error.getField(), new TreeSet<>());
            messages.add(error.getDefaultMessage());
            errors.put(error.getField(), messages);
        }
        return ResponseEntity.badRequest().body(new InvalidRequestResponse(errors));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(Exception ex, WebRequest request) {
        ex.printStackTrace();
        Map<String, String> body = new HashMap<>();
        body.put("message", "unknown error, please try again later !");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
