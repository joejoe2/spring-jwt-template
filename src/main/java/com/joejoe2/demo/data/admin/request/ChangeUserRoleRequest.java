package com.joejoe2.demo.data.admin.request;

import lombok.Data;

@Data
public class ChangeUserRoleRequest {
    private String id; //target user
    private String role; //target role
}
