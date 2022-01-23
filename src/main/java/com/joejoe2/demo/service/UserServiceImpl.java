package com.joejoe2.demo.service;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.Role;
import com.joejoe2.demo.model.User;
import com.joejoe2.demo.repository.UserRepository;
import com.joejoe2.demo.validation.EmailValidator;
import com.joejoe2.demo.validation.PasswordValidator;
import com.joejoe2.demo.validation.UserNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// @Service should be placed on implementation !!!
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(String username, String password, String email, Role role) throws ValidationError, AlreadyExist {
        UserNameValidator userNameValidator=new UserNameValidator(username);
        userNameValidator.validate();
        PasswordValidator passwordValidator=new PasswordValidator(password);
        passwordValidator.validate();
        EmailValidator emailValidator=new EmailValidator(email);
        emailValidator.validate();

        if (userRepository.getByUserName(userNameValidator.username).isPresent()||userRepository.getByEmail(emailValidator.email).isPresent())
            throw new AlreadyExist("username or email is already taken !");

        User user=new User();
        user.setUserName(userNameValidator.username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(emailValidator.email);
        user.setRole(role);
        userRepository.save(user);

        return user;
    }

    @Retryable(value = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changeRoleOf(String userId, Role role) throws InvalidOperation {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(()->new InvalidOperation("user is not exist !"));
        Role originalRole = user.getRole();
        if (role.equals(originalRole))throw new InvalidOperation("role doesn't change !");

        user.setRole(role);
        userRepository.save(user);

        if (Role.ADMIN.equals(originalRole)&&userRepository.getByRole(Role.ADMIN).size()==0)throw new InvalidOperation("cannot change the role of the only ADMIN !");
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
