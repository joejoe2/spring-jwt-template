package com.joejoe2.demo.data.admin.request;

import com.joejoe2.demo.validation.constraint.UUID;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserIdRequest {
    @UUID(message = "invalid user id !")
    @NotEmpty(message = "user id cannot be empty !")
    private String id;
}
