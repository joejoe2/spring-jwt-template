package com.joejoe2.demo.data.auth;

import com.joejoe2.demo.model.Role;
import com.joejoe2.demo.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class UserDetail implements UserDetails {
    private String id;
    private String currentAccessToken;
    private String username;
    private String password;
    private boolean isActive;
    private List<GrantedAuthority> authorities;

    public UserDetail(User user) {
        this.id=user.getId().toString();
        this.username = user.getUserName();
        this.password = user.getPassword();
        this.isActive = user.isActive();
        this.authorities = (List<GrantedAuthority>)mapRolesToAuthorities(Collections.singleton(user.getRole()));
    }

    public UserDetail(String id, String username, boolean isActive, Role role, String currentAccessToken) {
        this.id=id;
        this.username = username;
        this.isActive = isActive;
        this.currentAccessToken = currentAccessToken;
        this.authorities = (List<GrantedAuthority>)mapRolesToAuthorities(Collections.singleton(role));
    }

    public String getId(){
        return this.id;
    }

    public String getCurrentAccessToken(){
        return currentAccessToken;
    }

    private Collection< ? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles){
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
