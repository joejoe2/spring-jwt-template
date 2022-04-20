package com.joejoe2.demo.data.admin.request;

import com.joejoe2.demo.validation.constraint.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdRequest {
    @UUID(message = "invalid user id !")
    @NotEmpty(message = "user id cannot be empty !")
    private String id;
}
