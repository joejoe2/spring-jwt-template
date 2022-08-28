package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.repository.verification.VerifyTokenRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import redis.embedded.RedisServer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

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
    User user;

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

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword(passwordEncoder.encode("pa55ward"));
        user.setRole(Role.NORMAL);
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteById(user.getId());
        verifyTokenRepository.deleteAll();
    }

    @Test
    void changePasswordOfWithIllegalArgument(){
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()-> passwordService.changePasswordOf("invalid_uid", "pa55ward", "pa55ward123"));
        assertThrows(IllegalArgumentException.class, ()-> passwordService.changePasswordOf(user.getId().toString(), "pa55ward", "invalid_password"));
    }

    @Test
    void changePasswordOfWithInvalidOperation(){
        //test with incorrect password
        assertThrows(InvalidOperation.class, ()-> passwordService.changePasswordOf(user.getId().toString(), "incorrect", "pa55ward"));
        //test if old password==new password
        assertThrows(InvalidOperation.class, ()-> passwordService.changePasswordOf(user.getId().toString(), "pa55ward", "pa55ward"));
    }

    @Test
    void changePasswordOfWithDoesNotExist(){
        UUID id = UUID.randomUUID();
        while (userRepository.existsById(id))
            id = UUID.randomUUID();
        //test with a not exist user
        UUID finalId = id;
        assertThrows(UserDoesNotExist.class, ()-> passwordService.changePasswordOf(finalId.toString(), "pa55ward", "pa55ward"));
    }

    @Test
    void changePasswordOf(){
        //test success
        assertDoesNotThrow(()->{
            passwordService.changePasswordOf(user.getId().toString(), "pa55ward", "pa55ward123");
            assertTrue(passwordEncoder.matches("pa55ward123", userRepository.findById(user.getId()).get().getPassword()));
        });
    }

    @Test
    void requestResetPasswordWithIllegalArgument(){
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->passwordService.requestResetPasswordToken("invalid email"));
    }

    @Test
    void requestResetPasswordWithDoesNotExist(){
        //test a not exist user email
        assertThrows(UserDoesNotExist.class, ()->passwordService.requestResetPasswordToken("not@email.com"));
    }

    @Test
    void requestResetPassword() {
        //test success
        assertDoesNotThrow(()->{
            assertEquals(user, passwordService.requestResetPasswordToken("test@email.com").getUser());
        });
    }

    @Test
    void resetPasswordWithIllegalArgument() {
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()->passwordService.resetPassword("", "**-*/"));
    }

    @Test
    void resetPasswordWithInvalidOperation() {
        user = userRepository.findById(user.getId()).get();
        //test an incorrect token
        assertThrows(InvalidOperation.class, ()->passwordService.resetPassword("invalid token", "pa55ward"));
        //test a disabled user
        VerifyToken token=new VerifyToken();
        token.setToken("12345678");
        token.setUser(user);
        token.setExpireAt(Instant.now().plusSeconds(600));
        verifyTokenRepository.save(token);
        user.setActive(false);
        userRepository.save(user);
        assertThrows(InvalidOperation.class, ()->passwordService.resetPassword(token.getToken(), "newPa55ward"));
    }

    @Test
    void resetPassword() {
        //test success
        VerifyToken token=new VerifyToken();
        token.setToken("12345678");
        token.setUser(user);
        token.setExpireAt(Instant.now().plusSeconds(600));
        verifyTokenRepository.save(token);
        assertDoesNotThrow(()->{
            passwordService.resetPassword(token.getToken(), "a12345678");
            assertTrue(passwordEncoder.matches("a12345678", userRepository.findById(user.getId()).get().getPassword()));
        });
    }
}