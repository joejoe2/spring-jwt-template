package com.joejoe2.demo.exception;

public class UserDoesNotExist extends Exception {
  public UserDoesNotExist(String message) {
    super(message);
  }
}
