package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.data.auth.UserDetail;
import org.springframework.security.core.AuthenticationException;

public interface LoginService {
  UserDetail login(String username, String password) throws AuthenticationException;
}
