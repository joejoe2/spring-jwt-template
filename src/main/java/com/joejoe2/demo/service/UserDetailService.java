package com.joejoe2.demo.service;

import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=userRepository.getByUserName(username).orElseThrow(()->new UsernameNotFoundException("user does not exist !"));
        return new UserDetail(user);
    }
}
