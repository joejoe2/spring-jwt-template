package com.joejoe2.demo.data.user;

import com.joejoe2.demo.model.Role;
import com.joejoe2.demo.model.User;
import lombok.Data;

@Data
public class UserProfile {
    String id;
    String username;
    String email;
    Role role;
    String registeredAt;

    public UserProfile(User user) {
        this.id = user.getId().toString();
        this.username = user.getUserName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.registeredAt = user.getCreateAt().toString();
    }
}
