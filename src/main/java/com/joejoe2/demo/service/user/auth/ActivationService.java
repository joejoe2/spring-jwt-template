package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;

public interface ActivationService {
    /**
     * activate user with userId
     *
     * @param userId target user id
     * @throws InvalidOperation if target user is already active,
     *                          or you are trying to activate yourself
     * @throws UserDoesNotExist if target user is not exist
     */
    void activateUser(String userId) throws InvalidOperation, UserDoesNotExist;

    /**
     * deactivate user with userId
     *
     * @param userId target user id, this will also revoke all access tokens related
     *               to the user(in order to logout user)
     * @throws InvalidOperation if target user is not exist, already inactive,
     *                          or you are trying to deactivate yourself
     * @throws UserDoesNotExist if target user is not exist
     */
    void deactivateUser(String userId) throws InvalidOperation, UserDoesNotExist;
}
