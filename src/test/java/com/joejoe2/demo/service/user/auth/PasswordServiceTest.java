package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.repository.verification.VerifyTokenRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import redis.embedded.RedisServer;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PasswordServiceTest {
    @Autowired
    PasswordService passwordService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    VerifyTokenRepository verifyTokenRepository;

    private static RedisServer redisServer;

    @BeforeAll
    static void beforeAll() {
        redisServer=RedisServer.builder().port(6370).setting("maxmemory 128M").build();
        redisServer.start();
    }

    @AfterAll
    static void afterAll() {
        redisServer.stop();
    }

    @Test
    @Transactional
    void changePasswordOfWithIllegalArgument(){
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword(passwordEncoder.encode("pa55ward"));
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()-> passwordService.changePasswordOf("invalid_uid", "pa55ward", "pa55ward123"));
        assertThrows(IllegalArgumentException.class, ()-> passwordService.changePasswordOf(user.getId().toString(), "pa55ward", "invalid_password"));
    }

    @Test
    @Transactional
    void changePasswordOfWithInvalidOperation(){
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword(passwordEncoder.encode("pa55ward"));
        user.setRole(Role.NORMAL);
        userRepository.save(user);

        //test with incorrect password
        assertThrows(InvalidOperation.class, ()-> passwordService.changePasswordOf(user.getId().toString(), "incorrect", "pa55ward"));
        //test if old password==new password
        assertThrows(InvalidOperation.class, ()-> passwordService.changePasswordOf(user.getId().toString(), "pa55ward", "pa55ward"));
    }
    @Test
    @Transactional
    void changePasswordOfWithDoesNotExist(){
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword(passwordEncoder.encode("pa55ward"));
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        userRepository.delete(user);
        //test with a not exist user
        assertThrows(UserDoesNotExist.class, ()-> passwordService.changePasswordOf(user.getId().toString(), "pa55ward", "pa55ward"));
    }

    @Test
    @Transactional
    void changePasswordOf(){
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword(passwordEncoder.encode("pa55ward"));
        user.setRole(Role.NORMAL);
        userRepository.save(user);

        //test success
        assertDoesNotThrow(()->{
            passwordService.changePasswordOf(user.getId().toString(), "pa55ward", "pa55ward123");
            assertTrue(passwordEncoder.matches("pa55ward123", userRepository.findById(user.getId()).get().getPassword()));
        });
    }

    @Test
    @Transactional
    void requestResetPasswordWithIllegalArgument(){
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->passwordService.requestResetPasswordToken("invalid email"));
    }

    @Test
    @Transactional
    void requestResetPasswordWithDoesNotExist(){
        //test a not exist user email
        assertThrows(UserDoesNotExist.class, ()->passwordService.requestResetPasswordToken("not@email.com"));
    }

    @Test
    @Transactional
    void requestResetPassword() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);
        //test success
        assertDoesNotThrow(()->{
            assertEquals(user, passwordService.requestResetPasswordToken("test@email.com").getUser());
        });
    }

    @Test
    @Transactional
    void resetPasswordWithIllegalArgument() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->passwordService.resetPassword("", "**-*/"));
    }

    @Test
    @Transactional
    void resetPasswordWithInvalidOperation() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test an incorrect token
        assertThrows(InvalidOperation.class, ()->passwordService.resetPassword("invalid token", "pa55ward"));
    }

    @Test
    @Transactional
    void resetPassword() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);
        //test success
        VerifyToken token=new VerifyToken();
        token.setToken("12345678");
        token.setUser(user);
        token.setExpireAt(LocalDateTime.now().plusMinutes(10));
        verifyTokenRepository.save(token);
        assertDoesNotThrow(()->{
            passwordService.resetPassword(token.getToken(), "a12345678");
            assertTrue(passwordEncoder.matches("a12345678", userRepository.findById(user.getId()).get().getPassword()));
        });
    }
}