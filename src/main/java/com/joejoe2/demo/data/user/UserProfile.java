package com.joejoe2.demo.data.user;

import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserProfile {
    @Schema(description = "id of the user")
    private String id;
    @Schema(description = "name of the user")
    private String username;
    @Schema(description = "email of the user")
    private String email;
    @Schema(description = "role of the user")
    private Role role;
    @Schema(description = "status of the user")
    private Boolean isActive;
    @Schema(description = "register time of the user")
    private String registeredAt;

    public UserProfile(User user) {
        this.id = user.getId().toString();
        this.username = user.getUserName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isActive = user.isActive();
        this.registeredAt = user.getCreateAt().toString();
    }
}
