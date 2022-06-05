package com.joejoe2.demo.data.admin.request;

import com.joejoe2.demo.validation.constraint.Role;
import com.joejoe2.demo.validation.constraint.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * request for change the role of target user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequest {
    /**
     * id of target user
     */
    @UUID(message = "invalid user id !")
    @NotEmpty(message = "user id cannot be empty !")
    private String id;

    /**
     * the role that target user want to change to
     */
    @Role
    private String role;
}
