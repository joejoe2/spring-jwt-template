package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import com.joejoe2.demo.repository.jwt.AccessTokenRepository;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.repository.verification.VerifyTokenRepository;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.utils.Utils;
import com.joejoe2.demo.validation.validator.EmailValidator;
import com.joejoe2.demo.validation.validator.PasswordValidator;
import com.joejoe2.demo.validation.validator.UUIDValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordServiceImpl implements PasswordService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtService jwtService;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Autowired
    VerifyTokenRepository verifyTokenRepository;
    EmailValidator emailValidator = EmailValidator.getInstance();
    PasswordValidator passwordValidator = PasswordValidator.getInstance();
    UUIDValidator uuidValidator = UUIDValidator.getInstance();

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changePasswordOf(String userId, String oldPassword, String newPassword) throws InvalidOperation, UserDoesNotExist {
        UUID id = uuidValidator.validate(userId);

        try {
            oldPassword = passwordValidator.validate(oldPassword);
        } catch (ValidationError e) {
            throw new InvalidOperation("old password is not correct !");
        }
        newPassword = passwordValidator.validate(newPassword);
        newPassword = passwordEncoder.encode(newPassword);

        User user = userRepository.findById(id).orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new InvalidOperation("old password is not correct !");
        if (passwordEncoder.matches(oldPassword, newPassword))
            throw new InvalidOperation("new password is same with old password !");

        user.setPassword(newPassword);
        userRepository.save(user);

        //need to delete the verify token for password reset
        //because the password has been changed
        verifyTokenRepository.deleteByUser(user);

        //need to logout user after password change
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public VerifyToken requestResetPasswordToken(String email) throws InvalidOperation, UserDoesNotExist {
        email = emailValidator.validate(email);

        User user = userRepository.getByEmail(email).orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        if (!user.isActive()) throw new InvalidOperation("User is disabled !");

        Optional<VerifyToken> token = verifyTokenRepository.getByUser(user);
        if (token.isPresent() && token.get().getExpireAt().isAfter(Instant.now()))
            throw new InvalidOperation("already request to reset password, please try again later !");
        if (token.isPresent() && token.get().getExpireAt().isBefore(Instant.now()))
            verifyTokenRepository.delete(token.get());  // REPEATABLE_READ, if deleted by others
        // , it will cause (ERROR:  could not serialize access due to concurrent delete)
        // instead of being ignored by postgresql in read-committed

        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setUser(user);
        verifyToken.setToken(Utils.randomNumericCode(128));
        //valid for 10 min
        verifyToken.setExpireAt(Instant.now().plusSeconds(600));
        verifyTokenRepository.save(verifyToken);

        return verifyToken;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public void resetPassword(String verifyToken, String newPassword) throws InvalidOperation {
        newPassword = passwordValidator.validate(newPassword);
        newPassword = passwordEncoder.encode(newPassword);

        VerifyToken token = verifyTokenRepository.getByTokenAndExpireAtGreaterThan(verifyToken, Instant.now())
                .orElseThrow(() -> new InvalidOperation("reset password request is invalid or expired, please try to use forget password again later !"));

        User user = token.getUser();
        if (!user.isActive()) throw new InvalidOperation("User is disabled !");

        user.setPassword(newPassword);
        userRepository.save(user);
        verifyTokenRepository.delete(token);// REPEATABLE_READ, if deleted by others
        // , it will cause (ERROR:  could not serialize access due to concurrent delete)
        // instead of being ignored by postgresql in read-committed

        //need to logout user after password change
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }
}
