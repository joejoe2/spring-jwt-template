package com.joejoe2.demo.utils;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class AuthUtilTest {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    User user;

    @BeforeEach
    void setUp() throws Exception {
        user=new User();
        user.setUserName("test");
        user.setPassword(new BCryptPasswordEncoder().encode("pa55ward"));
        user.setEmail("test@email.com");
        user.setRole(Role.NORMAL);
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteById(user.getId());
    }

    @Test
    void authenticate() {
        //test a not exist username
        assertThrows(AuthenticationException.class, ()->AuthUtil.authenticate(authenticationManager, "not exist", "pa55ward"));
        assertFalse(()->AuthUtil.isAuthenticated());
        assertThrows(AuthenticationException.class, ()->AuthUtil.currentUserDetail());
        //test with incorrect password
        assertThrows(AuthenticationException.class, ()->AuthUtil.authenticate(authenticationManager, "not exist", "12345678"));
        assertFalse(()->AuthUtil.isAuthenticated());
        assertThrows(AuthenticationException.class, ()->AuthUtil.currentUserDetail());

        //test with correct username and password
        AuthUtil.authenticate(authenticationManager, "test", "pa55ward");
        assertTrue(()->AuthUtil.isAuthenticated());
        assertDoesNotThrow(()->AuthUtil.currentUserDetail());
    }
}