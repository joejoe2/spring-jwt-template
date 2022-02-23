package com.joejoe2.demo.data.user;

import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import lombok.Data;

@Data
public class UserProfile {
    private String id;
    private String username;
    private String email;
    private Role role;
    private Boolean isActive;
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
