package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.config.LoginConfig;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.Instant;

@Data
@Embeddable
public class LoginAttempt {
    @Column(nullable = false, columnDefinition = "integer default 0")
    int attempts;

    @Column(nullable = true)
    Instant lastAttempt;

    public boolean isExceedLimit(LoginConfig loginConfig){
        return getAttempts() >= loginConfig.getMaxAttempts();
    }

    public boolean canAttempt(LoginConfig loginConfig){
        if(getLastAttempt() != null && getLastAttempt().plusSeconds(loginConfig.getCoolTime()).isBefore(Instant.now())){
            return true;
        }
        return !isExceedLimit(loginConfig);
    }

    public void attemptSuccess(LoginConfig loginConfig){
        if (!canAttempt(loginConfig)) throw new RuntimeException("cannot attempt !");
        setAttempts(0);
        setLastAttempt(Instant.now());
    }

    public void attemptFail(LoginConfig loginConfig){
        if (!canAttempt(loginConfig)) throw new RuntimeException("cannot attempt !");
        if (isExceedLimit(loginConfig)) setAttempts(0);
        setAttempts(getAttempts()+1);
        setLastAttempt(Instant.now());
    }
}
