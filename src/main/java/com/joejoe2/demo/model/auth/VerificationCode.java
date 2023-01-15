package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.utils.Utils;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
public class VerificationCode {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id;

    @Column(length = 128, nullable = false)
    private String email;

    @Column(length = 5, nullable = false)
    private String code = Utils.randomNumericCode(5);

    @Column(updatable = false, nullable = false)
    private Instant expireAt;
}
