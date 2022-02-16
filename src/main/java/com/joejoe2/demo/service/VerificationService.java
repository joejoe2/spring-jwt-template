package com.joejoe2.demo.service;

import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.ValidationError;

public interface VerificationService {
    public VerificationPair issueVerificationCode(String email);

    void verify(String key, String email, String code) throws InvalidOperation;
}
