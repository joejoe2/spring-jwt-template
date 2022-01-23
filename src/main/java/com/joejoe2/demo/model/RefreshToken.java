package com.joejoe2.demo.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.Version;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, updatable = false, nullable = false, columnDefinition="TEXT")
    private String token;

    @Column(updatable = false, nullable = false)
    private LocalDateTime expireAt;

    @OneToOne(/*cascade = CascadeType.REMOVE,*/ fetch = FetchType.LAZY) //if delete this => also delete accessToken
    @OnDelete(action = OnDeleteAction.CASCADE) //if delete accessToken => also delete this
    private AccessToken accessToken;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
