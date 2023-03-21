package com.joejoe2.demo.validation.validator;

import com.joejoe2.demo.exception.ValidationError;

public interface Validator<O, I> {
  O validate(I data) throws ValidationError;
}
