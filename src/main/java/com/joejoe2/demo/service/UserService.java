package com.joejoe2.demo.service;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.VerifyToken;

import java.util.List;

public interface UserService {
    /**
     * create a user with given params
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
     * @param username
     * @param password
     * @param email
     * @param verification verification info
     * @return created user
     * @throws AlreadyExist if target user(username or email) is already taken
     * @throws InvalidOperation if you do not pass the verification via VerificationService
     */
    User registerUser(String username, String password, String email, VerificationPair verification) throws AlreadyExist, InvalidOperation;

    /**
     * activate user with userId
     * @param userId target user id
     * @throws InvalidOperation if target user is not exist, already active,
     * or you are trying to activate yourself
     */
    void activateUser(String userId) throws InvalidOperation;

    /**
     * deactivate user with userId
     * @param userId target user id, this will also revoke all access tokens related
     * to the user(in order to logout user)
     * @throws InvalidOperation if target user is not exist, already inactive,
     * or you are trying to deactivate yourself
     */
    void deactivateUser(String userId) throws InvalidOperation;

    /**
     * change the role of user with userId, this will also revoke all access tokens related
     * to the user(in order to logout user)
     * @param userId target user id
     * @param role target role you want to change to
     * @throws InvalidOperation if target user is not exist, already in that role,
     * you are trying to change the role of yourself, or the target user is the only admin in db
     */
    void changeRoleOf(String userId, Role role) throws InvalidOperation;

    /**
     * change the password of user with userId, this will also revoke all access tokens related
     * to the user(in order to logout user)
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @throws InvalidOperation if target user is not exist, old password is not correct, old password
     * equals to new password
     */
    void changePasswordOf(String userId, String oldPassword, String newPassword) throws InvalidOperation;

    /**
     * generate VerifyToken related to the email (VerifyToken has an expiration time).
     * this will provide verification of resetPassword
     * @param email
     * @return VerifyToken, is used for verification of resetPassword
     * @throws InvalidOperation if target user(email) is not exist, target user is inactive,
     * or there is a non-expired VerifyToken already in db
     */
    VerifyToken requestResetPasswordToken(String email) throws InvalidOperation;

    /**
     * reset password of the related user to a non-expired verifyToken in db, this will also revoke all access tokens related
     * to the user(in order to logout user) and the verifyToken will be deleted after password reset
     * @param verifyToken
     * @param newPassword
     * @throws InvalidOperation if target user is inactive or there is not any active VerifyToken in db
     */
    void resetPassword(String verifyToken, String newPassword) throws InvalidOperation;

    /**
     * load UserProfile from db with given id in userDetail
     * @param userDetail
     * @return
     * @throws InvalidOperation if user id in userDetail is not exist in db
     */
    UserProfile getProfile(UserDetail userDetail) throws InvalidOperation;

    /**
     * get all user profiles from db
     * @return all user profiles
     */
    List<UserProfile> getAllUserProfiles();

    /**
     * get all user profiles from db with page request
     * @param page must>=0
     * @param size must>0
     * @return paged user profiles
     */
    PageList<UserProfile> getAllUserProfilesWithPage(int page, int size);
}
