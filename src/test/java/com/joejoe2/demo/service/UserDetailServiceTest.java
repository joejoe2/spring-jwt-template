package com.joejoe2.demo.service;

import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserDetailServiceTest {
    @Autowired
    UserDetailService userDetailService;
    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void loadUserByUsername() {
        assertThrows(UsernameNotFoundException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userDetailService.loadUserByUsername("not exist name");
            }
        });

        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        assertEquals(new UserDetail(user), userDetailService.loadUserByUsername(user.getUserName()));
    }
}