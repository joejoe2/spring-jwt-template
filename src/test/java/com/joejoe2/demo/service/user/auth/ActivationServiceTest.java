package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.utils.AuthUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
    void activateUserWithIllegalArgument() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()-> activationService.activateUser("invalid uid"));
    }

    @Test
    @Transactional
    void activateUserWithInvalidOperation() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test if target is already active
        assertThrows(InvalidOperation.class, ()-> activationService.activateUser(user.getId().toString()));
        //test if user try to activate himself
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(user));
        assertThrows(InvalidOperation.class, ()-> activationService.activateUser(user.getId().toString()));
        //clear mock login
        mockedStatic.close();
    }

    @Test
    @Transactional
    void activateUser(){
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        user.setActive(false);
        userRepository.save(user);
        //test success
        assertDoesNotThrow(()-> activationService.activateUser(user.getId().toString()));
        assertTrue(user.isActive());
    }

    @Test
    @Transactional
    void deactivateUserWithIllegalArgument() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, ()-> activationService.activateUser("invalid uid"));
    }

    @Test
    @Transactional
    void deactivateUserWithInvalidOperation() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test deactivate an admin
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        assertThrows(InvalidOperation.class, ()-> activationService.deactivateUser(user.getId().toString()));
        //test if target is already inactive
        user.setRole(Role.NORMAL);
        user.setActive(false);
        userRepository.save(user);
        assertThrows(InvalidOperation.class, ()-> activationService.deactivateUser(user.getId().toString()));
        //test if user try to activate himself
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(user));
        assertThrows(InvalidOperation.class, ()-> activationService.deactivateUser(user.getId().toString()));
        //clear mock login
        mockedStatic.close();
    }

    @Test
    @Transactional
    void deactivateUser() {
        User user=new User();
        user.setUserName("test");
        user.setPassword("pa55ward");
        user.setEmail("test@email.com");
        userRepository.save(user);

        //test success
        assertDoesNotThrow(()-> activationService.deactivateUser(user.getId().toString()));
        assertFalse(user.isActive());
    }
}