package com.joejoe2.demo.data.auth;

import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetail implements UserDetails {
  private String id;
  private String currentAccessToken;
  private String username;
  private String password;
  private boolean isActive;
  private Role role;
  private List<GrantedAuthority> authorities;

  public UserDetail(User user) {
    this.id = user.getId().toString();
    this.username = user.getUserName();
    this.password = user.getPassword();
    this.isActive = user.isActive();
    this.role = user.getRole();
    this.authorities =
        (List<GrantedAuthority>) mapRolesToAuthorities(Collections.singleton(user.getRole()));
  }

  public UserDetail(
      String id, String username, boolean isActive, Role role, String currentAccessToken) {
    this.id = id;
    this.username = username;
    this.isActive = isActive;
    this.currentAccessToken = currentAccessToken;
    this.role = role;
    this.authorities = (List<GrantedAuthority>) mapRolesToAuthorities(Collections.singleton(role));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserDetail)) return false;
    UserDetail that = (UserDetail) o;
    return isActive == that.isActive
        && id.equals(that.id)
        && username.equals(that.username)
        && role == that.role
        && authorities.equals(that.authorities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, isActive, role, authorities);
  }

  public String getId() {
    return this.id;
  }

  public boolean isActive() {
    return this.isActive;
  }

  public Role getRole() {
    return this.role;
  }

  public String getCurrentAccessToken() {
    return currentAccessToken;
  }

  private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.toString()))
        .collect(Collectors.toList());
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
