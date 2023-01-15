package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.utils.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    @Id
    //@GeneratedValue(generator = "UUID")
    //@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(updatable = false, nullable = false)
    private Instant expireAt;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public AccessToken(JwtConfig jwtConfig, User user) {
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, jwtConfig.getAccessTokenLifetimeSec());
        this.token = JwtUtil.generateAccessToken(jwtConfig.getPrivateKey(), getId().toString(), jwtConfig.getIssuer(), user, exp);
        this.expireAt = exp.toInstant();
        this.user = user;
    }
}
