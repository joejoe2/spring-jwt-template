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
    @Order(1)
    @Transactional
    void issueVerificationCode() {
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->verificationService.issueVerificationCode("not a email"));
        //test whether verification code is created
        VerificationPair verificationPair = verificationService.issueVerificationCode("test@email.com");
        assertEquals(verificationPair.getCode(), verificationCodeRepository.findById(UUID.fromString(verificationPair.getKey())).get().getCode());
    }

    @Test
    @Order(2)
    @Transactional
    void verify() {
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->verificationService.verify("invalid key", "test@email.com", "1234"));
        assertThrows(IllegalArgumentException.class, ()->verificationService.verify(UUID.randomUUID().toString(), "not a email", "1234"));
        assertThrows(IllegalArgumentException.class, ()->verificationService.verify(UUID.randomUUID().toString(), "test@email.com", null));
        //test verification fail with not exist code
        assertThrows(InvalidOperation.class, ()->verificationService.verify(UUID.randomUUID().toString(), "test@email.com", "1234"));
        //test verification with exist code
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail("test@email.com");
        verificationCode.setCode("1234");
        verificationCode.setExpireAt(LocalDateTime.now().plusHours(1));
        verificationCodeRepository.save(verificationCode);
        assertDoesNotThrow(()->verificationService.verify(verificationCode.getId().toString(), verificationCode.getEmail(), verificationCode.getCode()));
    }
}