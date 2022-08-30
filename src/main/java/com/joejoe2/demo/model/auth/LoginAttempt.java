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

    public void reset(){
        setAttempts(0);
    }

    public LoginAttempt(int attempts, Instant lastAttempt) {
        this.attempts = attempts;
        this.lastAttempt = lastAttempt;
    }

    public LoginAttempt() {
    }
}
