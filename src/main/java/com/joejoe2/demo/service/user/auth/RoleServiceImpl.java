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
public class RoleServiceImpl implements RoleService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtService jwtService;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    UUIDValidator uuidValidator = new UUIDValidator();

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changeRoleOf(String userId, Role role) throws InvalidOperation, UserDoesNotExist {
        UUID id = uuidValidator.validate(userId);

        User user = userRepository.findById(id).orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        if (AuthUtil.isAuthenticated() && AuthUtil.currentUserDetail().getId().equals(id.toString()))
            throw new InvalidOperation("cannot change the role of yourself !");
        Role originalRole = user.getRole();
        if (role.equals(originalRole)) throw new InvalidOperation("role doesn't change !");

        user.setRole(role);
        userRepository.save(user);

        if (Role.ADMIN.equals(originalRole) && userRepository.getByRole(Role.ADMIN).size() == 0)
            throw new InvalidOperation("cannot change the role of the only ADMIN !");

        //need to logout user after role change
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }
}
