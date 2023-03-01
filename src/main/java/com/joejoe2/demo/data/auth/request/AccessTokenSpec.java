package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.model.auth.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AccessTokenSpec {
    @Schema(description = "user id")
    String id;

    @Schema(description = "username")
    String username;

    @Schema(description = "role of user", implementation = Role.class)
    String role;

    @Schema(description = "state of user")
    Boolean isActive;

    @Schema(description = "expiration date in number of seconds since Epoch")
    long exp;

    @Schema(description = "issuer")
    String iss;

    @Schema(description = "access token id")
    String jti;

    @Schema(description = "token type", example = "access_token")
    String type;
}
