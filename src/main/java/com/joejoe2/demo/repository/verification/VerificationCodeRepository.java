package com.joejoe2.demo.repository.verification;

import com.joejoe2.demo.model.auth.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    void deleteByExpireAtLessThan(Instant dateTime);

    long deleteByIdAndEmailAndCodeAndExpireAtGreaterThan(UUID id, String email, String code, Instant dateTime);
}
