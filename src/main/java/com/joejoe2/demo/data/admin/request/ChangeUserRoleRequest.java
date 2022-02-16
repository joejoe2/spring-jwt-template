package com.joejoe2.demo.data.admin.request;

import com.joejoe2.demo.validation.constraint.UUID;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ChangeUserRoleRequest {
    @UUID(message = "invalid user id !")
    @NotEmpty(message = "user id cannot be empty !")
    private String id;

    @NotEmpty(message = "role cannot be empty !")
    private String role; //target role
}
