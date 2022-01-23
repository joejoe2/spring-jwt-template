package com.joejoe2.demo.service;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.User;
import com.joejoe2.demo.model.Role;

import java.util.List;

public interface UserService {
    public User createUser(String username, String password, String email, Role role) throws AlreadyExist, ValidationError;

    public void changeRoleOf(String userId, Role role) throws InvalidOperation;

    public UserProfile getProfile(UserDetail userDetail) throws InvalidOperation;

    public List<UserProfile> getAllUserProfiles();

    PageList<UserProfile> getAllUserProfilesWithPage(int page, int size) throws InvalidOperation;
}
