package com.joejoe2.demo.service.user.auth;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.utils.AuthUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class RoleServiceTest {
    @Autowired
    RoleService roleService;
    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void changeRoleOfWithIllegalArgument() {
        // test IllegalArgument
        assertThrows(IllegalArgumentException.class, () -> roleService.changeRoleOf("invalid_uid", Role.ADMIN));
        // test with not exist user
        assertThrows(UserDoesNotExist.class, () -> roleService.changeRoleOf(UUID.randomUUID().toString(), Role.STAFF));
        // test the only(default) admin
        UUID id = userRepository.getByRole(Role.ADMIN).get(0).getId();
        assertThrows(InvalidOperation.class, () -> roleService.changeRoleOf(id.toString(), Role.NORMAL));
    }

    @Test
    @Transactional
    void changeRoleOfWithDoesNotExist() {
        // test with not exist user
        assertThrows(UserDoesNotExist.class, () -> roleService.changeRoleOf(UUID.randomUUID().toString(), Role.STAFF));
    }

    @Test
    void changeRoleOfWithInvalidOperation() {
        // test the only(default) admin
        User user = userRepository.getByRole(Role.ADMIN).get(0);
        assertThrows(InvalidOperation.class, () -> roleService.changeRoleOf(user.getId().toString(), Role.NORMAL));
        //test if role does not change
        assertThrows(InvalidOperation.class, () -> roleService.changeRoleOf(user.getId().toString(), Role.ADMIN));
        //test if user try to change himself
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(user));
        assertThrows(InvalidOperation.class, ()-> roleService.changeRoleOf(user.getId().toString(), Role.NORMAL));
        //clear mock login
        mockedStatic.close();
    }

    @Test
    @Transactional void changeRoleOf(){
        // test with exist user
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        User finalUser1 = user;
        assertDoesNotThrow(()-> roleService.changeRoleOf(finalUser1.getId().toString(), Role.STAFF));
        user = userRepository.findById(user.getId()).get();
        assertEquals(user.getRole(), Role.STAFF);
        User finalUser = user;
        assertDoesNotThrow(()-> roleService.changeRoleOf(finalUser.getId().toString(), Role.ADMIN));
        user = userRepository.findById(user.getId()).get();
        assertEquals(user.getRole(), Role.ADMIN);
    }

}