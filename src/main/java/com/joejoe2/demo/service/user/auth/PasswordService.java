package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.VerifyToken;

public interface PasswordService {
    /**
     * change the password of user with userId, this will also revoke all access tokens related
     * to the user(in order to logout user)
     *
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @throws InvalidOperation if target user is old password is not correct, old password
     *                          equals to new password
     * @throws UserDoesNotExist if target user is not exist
     */
    void changePasswordOf(String userId, String oldPassword, String newPassword) throws InvalidOperation, UserDoesNotExist;

    /**
     * generate VerifyToken related to the email (VerifyToken has an expiration time).
     * this will provide verification of resetPassword
     *
     * @param email
     * @return VerifyToken, is used for verification of resetPassword
     * @throws InvalidOperation if target user is inactive,
     *                          or there is a non-expired VerifyToken already in db
     * @throws UserDoesNotExist if target user is not exist
     */
    VerifyToken requestResetPasswordToken(String email) throws InvalidOperation, UserDoesNotExist;

    /**
     * reset password of the related user to a non-expired verifyToken in db, this will also revoke all access tokens related
     * to the user(in order to logout user) and the verifyToken will be deleted after password reset
     *
     * @param verifyToken
     * @param newPassword
     * @throws InvalidOperation if target user is inactive or there is not any active VerifyToken in db
     */
    void resetPassword(String verifyToken, String newPassword) throws InvalidOperation;
}
