package com.joejoe2.demo.service.verification;

import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.auth.VerificationCode;
import com.joejoe2.demo.repository.verification.VerificationCodeRepository;
import com.joejoe2.demo.service.verification.VerificationService;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class VerificationServiceTest {
    @Autowired
    VerificationService verificationService;
    @Autowired
    VerificationCodeRepository verificationCodeRepository;

    @Test
    void issueVerificationCodeWithIllegalArgument() {
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->verificationService.issueVerificationCode("not a email"));
    }

    @Test
    @Transactional
    void issueVerificationCode() {
        //test whether verification code is created
        VerificationPair verificationPair = verificationService.issueVerificationCode("test@email.com");
        assertEquals(verificationPair.getCode(), verificationCodeRepository.findById(UUID.fromString(verificationPair.getKey())).get().getCode());
    }

    @Test
    void verifyWithIllegalArgument() {
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->verificationService.verify("invalid key", "test@email.com", "1234"));
        assertThrows(IllegalArgumentException.class, ()->verificationService.verify(UUID.randomUUID().toString(), "not a email", "1234"));
        assertThrows(IllegalArgumentException.class, ()->verificationService.verify(UUID.randomUUID().toString(), "test@email.com", null));
    }

    @Test
    void verifyWithInvalidOperation() {
        //test verification fail with not exist code
        assertThrows(InvalidOperation.class, ()->verificationService.verify(UUID.randomUUID().toString(), "test@email.com", "1234"));
    }

    @Test
    @Transactional
    void verify() {
        //test verification with exist code
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail("test@email.com");
        verificationCode.setCode("1234");
        verificationCode.setExpireAt(Instant.now().plusSeconds(3600));
        verificationCodeRepository.save(verificationCode);
        assertDoesNotThrow(()->verificationService.verify(verificationCode.getId().toString(), verificationCode.getEmail(), verificationCode.getCode()));
    }
}