package com.joejoe2.demo.service;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.Role;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    User createUser(String username, String password, String email, Role role) throws AlreadyExist;

    @Transactional(rollbackFor = Exception.class)
    User registerUser(String username, String password, String email, VerificationPair verification) throws AlreadyExist, InvalidOperation;

    void changeRoleOf(String userId, Role role) throws InvalidOperation;

    void changePasswordOf(String userId, String oldPassword, String newPassword) throws InvalidOperation;

    UserProfile getProfile(UserDetail userDetail) throws InvalidOperation;

    List<UserProfile> getAllUserProfiles();

    PageList<UserProfile> getAllUserProfilesWithPage(int page, int size) throws InvalidOperation;
}
