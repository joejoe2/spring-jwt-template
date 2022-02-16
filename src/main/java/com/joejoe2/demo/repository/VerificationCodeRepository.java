package com.joejoe2.demo.repository;

import com.joejoe2.demo.model.auth.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    void deleteByExpireAtLessThan(LocalDateTime dateTime);
    long deleteByIdAndEmailAndCodeAndExpireAtGreaterThan(UUID id, String email, String code, LocalDateTime dateTime);
}
