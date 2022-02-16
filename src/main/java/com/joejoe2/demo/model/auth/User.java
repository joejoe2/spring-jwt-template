package com.joejoe2.demo.model.auth;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "account_user")
public class User{
    @Version
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
    private Timestamp version;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, length = 32, nullable = false)
    private String userName;

    @Column(length = 64, nullable = false)
    private String password;

    @Column(unique = true, length = 128, nullable = false)
    private String email;

    @Column(length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role=Role.NORMAL; //code level default

    @Column(nullable = false, columnDefinition = "boolean default true") //db level default
    private boolean isActive=true; //code level default

    @CreationTimestamp
    private LocalDateTime createAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;
}