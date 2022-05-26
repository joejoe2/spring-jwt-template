package com.joejoe2.demo.service.verification;

import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.auth.VerificationCode;
import com.joejoe2.demo.repository.verification.VerificationCodeRepository;
import com.joejoe2.demo.repository.verification.VerifyTokenRepository;
import com.joejoe2.demo.service.email.EmailService;
import com.joejoe2.demo.validation.servicelayer.EmailValidator;
import com.joejoe2.demo.validation.servicelayer.UUIDValidator;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.UUID;

@Service
public class VerificationServiceImpl implements VerificationService {
    @Autowired
    VerificationCodeRepository verificationRepository;
    @Autowired
    VerifyTokenRepository verifyTokenRepository;
    @Autowired
    EmailService emailService;

    @Override
    public VerificationPair issueVerificationCode(String email){
        email = new EmailValidator().validate(email);

        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 300);
        VerificationCode emailVerification = new VerificationCode();
        emailVerification.setEmail(email);
        emailVerification.setExpireAt(exp.toInstant());
        verificationRepository.save(emailVerification);

        emailService.sendSimpleEmail(emailVerification.getEmail(), "Verification", "your verification code is "+emailVerification.getCode());
        return new VerificationPair(emailVerification.getId().toString(), emailVerification.getCode());
    }

    @Override
    public void verify(String key, String email, String code) throws InvalidOperation {
        UUID keyId = new UUIDValidator().validate(key);
        email = new EmailValidator().validate(email);
        if (code==null)throw new ValidationError("code cannot be null !");

        if(verificationRepository.deleteByIdAndEmailAndCodeAndExpireAtGreaterThan(keyId, email, code, Instant.now())==0){
            throw new InvalidOperation("verification fail !");
        }
    }

    @Job(name = "delete all expired verification codes")
    @Transactional // jobrunr error
    @Override
    public void deleteExpiredVerificationCodes() {
        verificationRepository.deleteByExpireAtLessThan(Instant.now());
    }

    @Job(name = "delete all expired verify tokens")
    @Transactional // jobrunr error
    @Override
    public void deleteExpiredVerifyTokens() {
        verifyTokenRepository.deleteByExpireAtLessThan(Instant.now());
    }
}
