package com.joejoe2.demo.service;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import com.joejoe2.demo.repository.AccessTokenRepository;
import com.joejoe2.demo.repository.UserRepository;
import com.joejoe2.demo.repository.VerifyTokenRepository;
import com.joejoe2.demo.utils.AuthUtil;
import com.joejoe2.demo.utils.Utils;
import com.joejoe2.demo.validation.servicelayer.EmailValidator;
import com.joejoe2.demo.validation.servicelayer.PasswordValidator;
import com.joejoe2.demo.validation.servicelayer.UUIDValidator;
import com.joejoe2.demo.validation.servicelayer.UserNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// @Service should be placed on implementation !!!
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    VerificationService verificationService;
    @Autowired
    JwtService jwtService;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Autowired
    VerifyTokenRepository verifyTokenRepository;

    @Override
    public User createUser(String username, String password, String email, Role role) throws ValidationError, AlreadyExist {
        username = new UserNameValidator().validate(username);
        password = new PasswordValidator().validate(password);
        email = new EmailValidator().validate(email);

        if (userRepository.getByUserName(username).isPresent()||userRepository.getByEmail(email).isPresent())
            throw new AlreadyExist("username or email is already taken !");

        User user=new User();
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(role);
        userRepository.save(user);

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User registerUser(String username, String password, String email, VerificationPair verificationPair) throws AlreadyExist, InvalidOperation {
        verificationService.verify(verificationPair.getKey(), email, verificationPair.getCode());
        return createUser(username, password, email, Role.NORMAL);
    }

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Override
    public void activateUser(String userId) throws InvalidOperation {
        UUID id = new UUIDValidator().validate(userId);

        User user = userRepository.findById(id).orElseThrow(()->new InvalidOperation("user is not exist !"));
        if (AuthUtil.isAuthenticated()&&AuthUtil.currentUserDetail().getId().equals(id.toString()))throw new InvalidOperation("cannot activate yourself !");
        if (user.isActive())throw new InvalidOperation("target user is already active !");

        user.setActive(true);
        userRepository.save(user);
    }

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deactivateUser(String userId) throws InvalidOperation {
        UUID id = new UUIDValidator().validate(userId);

        User user = userRepository.findById(id).orElseThrow(()->new InvalidOperation("user is not exist !"));
        if (AuthUtil.isAuthenticated()&&AuthUtil.currentUserDetail().getId().equals(id.toString()))throw new InvalidOperation("cannot deactivate yourself !");
        if (user.getRole()==Role.ADMIN)throw new InvalidOperation("cannot deactivate an admin !");
        if (!user.isActive())throw new InvalidOperation("target user is already inactive !");

        user.setActive(false);
        userRepository.save(user);

        //need to logout user after deactivate
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changeRoleOf(String userId, Role role) throws InvalidOperation {
        UUID id = new UUIDValidator().validate(userId);

        User user = userRepository.findById(id).orElseThrow(()->new InvalidOperation("user is not exist !"));
        if (AuthUtil.isAuthenticated()&&AuthUtil.currentUserDetail().getId().equals(id.toString()))throw new InvalidOperation("cannot change the role of yourself !");
        Role originalRole = user.getRole();
        if (role.equals(originalRole))throw new InvalidOperation("role doesn't change !");

        user.setRole(role);
        userRepository.save(user);

        if (Role.ADMIN.equals(originalRole)&&userRepository.getByRole(Role.ADMIN).size()==0)throw new InvalidOperation("cannot change the role of the only ADMIN !");

        //need to logout user after role change
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changePasswordOf(String userId, String oldPassword, String newPassword) throws InvalidOperation {
        UUID id = new UUIDValidator().validate(userId);
        PasswordValidator passwordValidator = new PasswordValidator();

        try {
            oldPassword = passwordValidator.validate(oldPassword);
        }catch (ValidationError e){
            throw new InvalidOperation("old password is not correct !");
        }
        newPassword = passwordValidator.validate(newPassword);
        newPassword = passwordEncoder.encode(newPassword);

        User user = userRepository.findById(id).orElseThrow(()->new InvalidOperation("user is not exist !"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))throw new InvalidOperation("old password is not correct !");
        if(passwordEncoder.matches(oldPassword, newPassword))throw new InvalidOperation("new password is same with old password !");

        user.setPassword(newPassword);
        userRepository.save(user);

        //need to logout user after password change
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public VerifyToken requestResetPassword(String email) throws InvalidOperation{
        email=new EmailValidator().validate(email);

        User user=userRepository.getByEmail(email).orElseThrow(()->new InvalidOperation("user is not exist !"));
        if (!user.isActive())throw new InvalidOperation("User is disabled !");

        Optional<VerifyToken> token=verifyTokenRepository.getByUser(user);
        if(token.isPresent()&&token.get().getExpireAt().isAfter(LocalDateTime.now()))
            throw new InvalidOperation("already request to reset password, please try again later !");
        if(token.isPresent()&&token.get().getExpireAt().isBefore(LocalDateTime.now()))
            verifyTokenRepository.delete(token.get());  // REPEATABLE_READ, if deleted by others
        // , it will cause (ERROR:  could not serialize access due to concurrent delete)
        // instead of being ignored by postgresql in read-committed

        VerifyToken verifyToken=new VerifyToken();
        verifyToken.setUser(user);
        verifyToken.setToken(Utils.randomNumericCode(128));
        //valid for 10 min
        verifyToken.setExpireAt(LocalDateTime.now().plusMinutes(10));
        verifyTokenRepository.save(verifyToken);

        return verifyToken;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public void resetPassword(String verifyToken, String newPassword) throws InvalidOperation{
        newPassword = new PasswordValidator().validate(newPassword);
        newPassword = passwordEncoder.encode(newPassword);

        VerifyToken token = verifyTokenRepository.getByTokenAndExpireAtGreaterThan(verifyToken, LocalDateTime.now())
                .orElseThrow(()->new InvalidOperation("reset password request is invalid or expired, please try to use forget password again later !"));

        User user=token.getUser();
        if (!user.isActive())throw new InvalidOperation("User is disabled !");

        user.setPassword(newPassword);
        userRepository.save(user);
        verifyTokenRepository.delete(token);// REPEATABLE_READ, if deleted by others
        // , it will cause (ERROR:  could not serialize access due to concurrent delete)
        // instead of being ignored by postgresql in read-committed

        //need to logout user after password change
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }

    @Override
    public UserProfile getProfile(UserDetail userDetail) throws InvalidOperation{
        User user = userRepository.findById(UUID.fromString(userDetail.getId())).orElseThrow(()->new InvalidOperation("user is not exist !"));
        return new UserProfile(user);
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return userRepository.findAll().stream().sorted(Comparator.comparing(User::getCreateAt)).map((user -> new UserProfile(user))).collect(Collectors.toList());
    }

    @Override
    public PageList<UserProfile> getAllUserProfilesWithPage(int page, int size) throws InvalidOperation{
        if (page<0||size<=0)throw new InvalidOperation("invalid page or size !");
        Page<User> paging =  userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createAt")));
        List<UserProfile> profiles = paging.getContent().stream().map((user -> new UserProfile(user))).collect(Collectors.toList());
        return new PageList<UserProfile>(paging.getTotalElements(), paging.getNumber(), paging.getTotalPages(), paging.getSize(), profiles);
    }
}
