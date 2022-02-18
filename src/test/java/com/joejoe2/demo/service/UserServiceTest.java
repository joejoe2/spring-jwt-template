package com.joejoe2.demo.service;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import redis.embedded.RedisServer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    UserService userService;
    @Autowired
    UserDetailService userDetailService;
    @Autowired
    UserRepository userRepository;

    //UserService need redis for changeRoleOf and changePasswordOf
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
    void createUser() {
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, () -> userService.createUser("**-@#", "pa55ward", "test@email.com", Role.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> userService.createUser("test", "**-@#", "test@email.com", Role.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> userService.createUser("test", "pa55ward", "not a email", Role.NORMAL));

        //test whether users are created
        User test1, test2, test3;
        try {
            test1 = userService.createUser("test1", "pa55ward", "test1@email.com", Role.NORMAL);
            test2 = userService.createUser("test2", "pa55ward", "test2@email.com", Role.MODERATE);
            test3 = userService.createUser("test3", "pa55ward", "test3@email.com", Role.ADMIN);
        }catch (Exception e){
            throw new AssertionError(e);
        }
        assertEquals("test1", test1.getUserName());
        assertEquals("test2", test2.getUserName());
        assertEquals("test3", test3.getUserName());
        assertEquals("test1@email.com", test1.getEmail());
        assertEquals("test2@email.com", test2.getEmail());
        assertEquals("test3@email.com", test3.getEmail());
        assertEquals(Role.NORMAL, test1.getRole());
        assertEquals(Role.MODERATE, test2.getRole());
        assertEquals(Role.ADMIN, test3.getRole());

        //test with duplicated username or email
        assertThrows(AlreadyExist.class, () -> userService.createUser("test1", "pa55ward", "test11@email.com", Role.NORMAL));
        assertThrows(AlreadyExist.class, () -> userService.createUser("test12", "pa55ward", "test1@email.com", Role.NORMAL));
    }

    @Test
    @Transactional
    void changeRoleOf() {
        //test IllegalArgument
        assertThrows(IllegalArgumentException.class, () -> userService.changeRoleOf("invalid_uid", Role.ADMIN));
        // test with not exist user
        assertThrows(InvalidOperation.class, () -> userService.changeRoleOf(UUID.randomUUID().toString(), Role.MODERATE));
        // test with exist user
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        User finalUser1 = user;
        assertDoesNotThrow(()->userService.changeRoleOf(finalUser1.getId().toString(), Role.MODERATE));
        user = userRepository.findById(user.getId()).get();
        assertEquals(user.getRole(), Role.MODERATE);
        User finalUser = user;
        assertDoesNotThrow(()->userService.changeRoleOf(finalUser.getId().toString(), Role.ADMIN));
        user = userRepository.findById(user.getId()).get();
        assertEquals(user.getRole(), Role.ADMIN);
        // test the only(default) admin
        userRepository.delete(user);
        UUID id = userRepository.getByRole(Role.ADMIN).get(0).getId();
        assertThrows(InvalidOperation.class, () -> userService.changeRoleOf(id.toString(), Role.NORMAL));
    }

    @Test
    @Transactional
    void getProfile() {
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        UserProfile profile;
        try {
            profile = userService.getProfile((UserDetail) userDetailService.loadUserByUsername(user.getUserName()));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        assertEquals(new UserProfile(user), profile);
    }

    @Test
    @Transactional
    void getAllUserProfilesWithPage() {
        //test IllegalArgument
        assertThrows(InvalidOperation.class, () -> userService.getAllUserProfilesWithPage(-1, 1));
        assertThrows(InvalidOperation.class, () -> userService.getAllUserProfilesWithPage(0, -1));
        assertThrows(InvalidOperation.class, () -> userService.getAllUserProfilesWithPage(0, 0));
        //test success
        PageList<UserProfile> pageList;
        try {
            pageList = userService.getAllUserProfilesWithPage(5, 10);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        assertEquals(5, pageList.getCurrentPage());
        assertEquals(10, pageList.getPageSize());
    }
}