package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;

public interface RoleService {
    /**
     * change the role of user with userId, this will also revoke all access tokens related
     * to the user(in order to logout user)
     * @param userId target user id
     * @param role target role you want to change to
     * @throws InvalidOperation if target user is already in that role,
     * you are trying to change the role of yourself, or the target user is the only admin in db
     * @throws UserDoesNotExist if target user is not exist
     */
    void changeRoleOf(String userId, Role role) throws InvalidOperation, UserDoesNotExist;
}
