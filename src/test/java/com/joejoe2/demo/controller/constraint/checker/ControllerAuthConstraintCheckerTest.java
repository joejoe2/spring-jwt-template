package com.joejoe2.demo.controller.constraint.checker;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.controller.constraint.auth.ApiAllowsTo;
import com.joejoe2.demo.controller.constraint.auth.ApiRejectTo;
import com.joejoe2.demo.controller.constraint.auth.AuthenticatedApi;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.ControllerConstraintViolation;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.utils.AuthUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class ControllerAuthConstraintCheckerTest {
    @Autowired
    ControllerAuthConstraintChecker checker;
    @Autowired
    UserRepository userRepository;

    class TestMethod {
        @AuthenticatedApi
        public void authenticated() {
        }

        @ApiAllowsTo(roles = {Role.ADMIN, Role.STAFF}, rejectStatus = 400)
        public void allow() {

        }

        @ApiRejectTo(roles = Role.NORMAL, rejectMessage = "test")
        public void reject() {

        }
    }

    TestMethod testMethod;
    User testUser, testStaff, testAdmin;

    @BeforeEach
    void setup() {
        testMethod = new TestMethod();

        testUser = new User();
        testUser.setUserName("testUser");
        testUser.setRole(Role.NORMAL);
        testUser.setEmail("testUser@email.com");
        testUser.setPassword("pa55ward");
        userRepository.save(testUser);

        testStaff = new User();
        testStaff.setUserName("testStaff");
        testStaff.setRole(Role.STAFF);
        testStaff.setEmail("testStaff@email.com");
        testStaff.setPassword("pa55ward");
        userRepository.save(testStaff);

        testAdmin = new User();
        testAdmin.setUserName("testAdmin");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setEmail("testAdmin@email.com");
        testAdmin.setPassword("pa55ward");
        userRepository.save(testAdmin);
    }

    @AfterEach
    void destroy() {
        userRepository.deleteById(testUser.getId());
        userRepository.deleteById(testStaff.getId());
        userRepository.deleteById(testAdmin.getId());
    }

    @Test
    void checkWithMethodForAuthenticatedApi() {
        //if does not login
        assertThrows(ControllerConstraintViolation.class, () -> checker.checkWithMethod(testMethod.getClass().getMethod("authenticated")));
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        assertDoesNotThrow(() -> checker.checkWithMethod(testMethod.getClass().getMethod("authenticated")));
        //clear mock login
        mockedStatic.close();
    }

    @Test
    void checkWithMethodForApiAllowsTo() throws Exception {
        //if does not login
        assertThrows(ControllerConstraintViolation.class, () -> checker.checkWithMethod(testMethod.getClass().getMethod("authenticated")));
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        //test violate
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(testUser));
        //test status code
        try {
            checker.checkWithMethod(testMethod.getClass().getMethod("allow"));
        } catch (ControllerConstraintViolation e) {
            assertEquals(400, e.getRejectStatus());
        }
        //test does not violate
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(testStaff));
        assertDoesNotThrow(() -> checker.checkWithMethod(testMethod.getClass().getMethod("allow")));
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(testAdmin));
        assertDoesNotThrow(() -> checker.checkWithMethod(testMethod.getClass().getMethod("allow")));
        //clear mock login
        mockedStatic.close();
    }

    @Test
    void checkWithMethodForApiRejectTo() throws Exception {
        //if does not login
        assertThrows(ControllerConstraintViolation.class, () -> checker.checkWithMethod(testMethod.getClass().getMethod("reject")));
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        //test violate
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(testUser));
        assertThrows(ControllerConstraintViolation.class, () -> checker.checkWithMethod(testMethod.getClass().getMethod("reject")));
        //test message
        try {
            checker.checkWithMethod(testMethod.getClass().getMethod("reject"));
        } catch (ControllerConstraintViolation e) {
            assertEquals("test", e.getRejectMessage());
        }
        //test does not violate
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(testStaff));
        assertDoesNotThrow(() -> checker.checkWithMethod(testMethod.getClass().getMethod("reject")));
        mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(testAdmin));
        assertDoesNotThrow(() -> checker.checkWithMethod(testMethod.getClass().getMethod("reject")));
        //clear mock login
        mockedStatic.close();
    }
}