package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RegistrationServiceTest {
    @Autowired
    RegistrationService registrationService;

    @Test
    @Transactional // roll back after test
    void createUserWithIllegalArgument() throws Exception{
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, () -> registrationService.createUser("**-@#", "pa55ward", "test@email.com", Role.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> registrationService.createUser("test", "**-@#", "test@email.com", Role.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> registrationService.createUser("test", "pa55ward", "not a email", Role.NORMAL));
    }

    @Test
    @Transactional // roll back after test
    void createUserWithAlreadyExist() throws Exception{
        //test with duplicated username or email
        registrationService.createUser("test1", "pa55ward", "test1@email.com", Role.NORMAL);
        assertThrows(AlreadyExist.class, () -> registrationService.createUser("test1", "pa55ward", "test11@email.com", Role.NORMAL));
        assertThrows(AlreadyExist.class, () -> registrationService.createUser("test12", "pa55ward", "test1@email.com", Role.NORMAL));
    }

    @Test
    @Transactional // roll back after test
    void createUser() throws Exception{
        //test whether users are created
        User test1, test2, test3;
        test1 = registrationService.createUser("test1", "pa55ward", "test1@email.com", Role.NORMAL);
        test2 = registrationService.createUser("test2", "pa55ward", "test2@email.com", Role.STAFF);
        test3 = registrationService.createUser("test3", "pa55ward", "test3@email.com", Role.ADMIN);

        assertEquals("test1", test1.getUserName());
        assertEquals("test2", test2.getUserName());
        assertEquals("test3", test3.getUserName());
        assertEquals("test1@email.com", test1.getEmail());
        assertEquals("test2@email.com", test2.getEmail());
        assertEquals("test3@email.com", test3.getEmail());
        assertEquals(Role.NORMAL, test1.getRole());
        assertEquals(Role.STAFF, test2.getRole());
        assertEquals(Role.ADMIN, test3.getRole());
    }
}