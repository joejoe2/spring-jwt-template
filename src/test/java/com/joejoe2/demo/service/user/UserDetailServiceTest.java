package com.joejoe2.demo.service.user;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class UserDetailServiceTest {
    @Autowired
    UserDetailService userDetailService;
    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void loadUserByNotFoundUsername() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailService.loadUserByUsername("not exist name"));
    }

    @Test
    @Transactional
    void loadUserByUsername() {
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        assertEquals(new UserDetail(user), userDetailService.loadUserByUsername(user.getUserName()));
    }
}