package com.joejoe2.demo.model.auth;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class RefreshToken {
    @Id
    //@GeneratedValue(generator = "UUID")
    //@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id=UUID.randomUUID();

    @Column(unique = true, updatable = false, nullable = false, columnDefinition="TEXT")
    private String token;

    @Column(updatable = false, nullable = false)
    private Instant expireAt;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, optional = false) //if delete this => also delete accessToken
    @OnDelete(action = OnDeleteAction.CASCADE) //if delete accessToken => also delete this
    private AccessToken accessToken;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
