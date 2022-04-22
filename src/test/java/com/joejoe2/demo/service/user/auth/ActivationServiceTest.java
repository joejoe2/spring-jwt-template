package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ActivationServiceTest {
    @Autowired
    ActivationService activationService;
    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void activateUser() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()-> activationService.activateUser("invalid uid"));
        //test if target is already active
        assertThrows(InvalidOperation.class, ()-> activationService.activateUser(user.getId().toString()));
        //test success
        user.setActive(false);
        userRepository.save(user);
        assertDoesNotThrow(()-> activationService.activateUser(user.getId().toString()));
        assertTrue(user.isActive());
    }

    @Test
    @Transactional
    void deactivateUser() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        user.setActive(false);
        userRepository.save(user);

        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()-> activationService.activateUser("invalid uid"));
        //test deactivate an admin
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        assertThrows(InvalidOperation.class, ()-> activationService.deactivateUser(user.getId().toString()));
        //test if target is already inactive
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        assertThrows(InvalidOperation.class, ()-> activationService.deactivateUser(user.getId().toString()));
        //test success
        user.setActive(true);
        userRepository.save(user);
        assertDoesNotThrow(()-> activationService.deactivateUser(user.getId().toString()));
        assertFalse(user.isActive());
    }
}