package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;

public interface RegistrationService {
    /**
     * create a user with given params
     *
     * @param username
     * @param password
     * @param email
     * @param role
     * @return created user
     * @throws AlreadyExist if target user(username or email) is already taken
     */
    User createUser(String username, String password, String email, Role role) throws AlreadyExist;

    /**
     * this will first verify verification info via VerificationService,
     * then create a user with given params if pass the verification
     *
     * @param username
     * @param password
     * @param email
     * @param verification verification info
     * @return created user
     * @throws AlreadyExist     if target user(username or email) is already taken
     * @throws InvalidOperation if you do not pass the verification via VerificationService
     */
    User registerUser(String username, String password, String email, VerificationPair verification) throws AlreadyExist, InvalidOperation;
}
