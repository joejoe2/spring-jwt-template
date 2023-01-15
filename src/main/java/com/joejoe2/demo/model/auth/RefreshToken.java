package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.utils.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    //@GeneratedValue(generator = "UUID")
    //@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(updatable = false, nullable = false)
    private Instant expireAt;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, optional = false)
    //if delete this => also delete accessToken
    @OnDelete(action = OnDeleteAction.CASCADE) //if delete accessToken => also delete this
    private AccessToken accessToken;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public RefreshToken(JwtConfig jwtConfig, AccessToken accessToken) {
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, jwtConfig.getRefreshTokenLifetimeSec());
        this.token = JwtUtil.generateRefreshToken(jwtConfig.getPrivateKey(), getId().toString(), jwtConfig.getIssuer(), exp);
        this.accessToken = accessToken;
        this.user = accessToken.getUser();
        this.expireAt = exp.toInstant();
    }
}
