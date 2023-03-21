package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.service.verification.VerificationService;
import com.joejoe2.demo.validation.validator.EmailValidator;
import com.joejoe2.demo.validation.validator.PasswordValidator;
import com.joejoe2.demo.validation.validator.UserNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationServiceImpl implements RegistrationService {
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired VerificationService verificationService;
  EmailValidator emailValidator = EmailValidator.getInstance();
  PasswordValidator passwordValidator = PasswordValidator.getInstance();
  UserNameValidator userNameValidator = UserNameValidator.getInstance();

  @Override
  public User createUser(String username, String password, String email, Role role)
      throws AlreadyExist {
    username = userNameValidator.validate(username);
    password = passwordValidator.validate(password);
    email = emailValidator.validate(email);

    if (userRepository.getByUserName(username).isPresent()
        || userRepository.getByEmail(email).isPresent())
      throw new AlreadyExist("username or email is already taken !");

    User user = new User();
    user.setUserName(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setEmail(email);
    user.setRole(role);
    userRepository.save(user);

    return user;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public User registerUser(
      String username, String password, String email, VerificationPair verificationPair)
      throws AlreadyExist, InvalidOperation {
    verificationService.verify(verificationPair.getKey(), email, verificationPair.getCode());
    return createUser(username, password, email, Role.NORMAL);
  }
}
