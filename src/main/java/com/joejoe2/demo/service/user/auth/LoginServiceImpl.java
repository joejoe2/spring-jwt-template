package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.config.LoginConfig;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.model.auth.LoginAttempt;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LoginConfig loginConfig;

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(noRollbackFor = AuthenticationException.class)
    @Override
    public UserDetail login(String username, String password) throws AuthenticationException {
        User user = userRepository.getByUserName(username)
                .orElseThrow(()->new UsernameNotFoundException("Username is not exist !"));

        LoginAttempt loginAttempt = user.getLoginAttempt();
        if (!loginAttempt.canAttempt(loginConfig)){
            throw new AuthenticationServiceException("You have try too many times, please try again later !");
        }

        try {
            UserDetail userDetail = AuthUtil.authenticate(authenticationManager, username, password);
            loginAttempt.reset();
            loginAttempt.setLastAttempt(Instant.now());
            userRepository.save(user);
            return userDetail;
        } catch (BadCredentialsException e){
            if (loginAttempt.isExceedLimit(loginConfig)) loginAttempt.reset();
            loginAttempt.setAttempts(loginAttempt.getAttempts()+1);
            loginAttempt.setLastAttempt(Instant.now());
            userRepository.save(user);
            throw e;
        }
    }
}
