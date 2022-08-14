package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.jwt.AccessTokenRepository;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.utils.AuthUtil;
import com.joejoe2.demo.validation.servicelayer.UUIDValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivationServiceImpl implements ActivationService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtService jwtService;
    @Autowired
    AccessTokenRepository accessTokenRepository;

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Override
    public void activateUser(String userId) throws InvalidOperation, UserDoesNotExist {
        UUID id = new UUIDValidator().validate(userId);

        User user = userRepository.findById(id).orElseThrow(()->new UserDoesNotExist("user is not exist !"));
        if (AuthUtil.isAuthenticated()&&AuthUtil.currentUserDetail().getId().equals(id.toString()))throw new InvalidOperation("cannot activate yourself !");
        if (user.isActive())throw new InvalidOperation("target user is already active !");

        user.setActive(true);
        userRepository.save(user);
    }

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deactivateUser(String userId) throws InvalidOperation, UserDoesNotExist {
        UUID id = new UUIDValidator().validate(userId);

        User user = userRepository.findById(id).orElseThrow(()->new UserDoesNotExist("user is not exist !"));
        if (AuthUtil.isAuthenticated()&&AuthUtil.currentUserDetail().getId().equals(id.toString()))throw new InvalidOperation("cannot deactivate yourself !");
        if (user.getRole()==Role.ADMIN)throw new InvalidOperation("cannot deactivate an admin !");
        if (!user.isActive())throw new InvalidOperation("target user is already inactive !");

        user.setActive(false);
        userRepository.save(user);

        //need to logout user after deactivate
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }
}
