package com.joejoe2.demo.controller.constraint.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.controller.constraint.rate.LimitTarget;
import com.joejoe2.demo.controller.constraint.rate.RateLimit;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.ControllerConstraintViolation;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.utils.AuthUtil;
import com.joejoe2.demo.utils.IPUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class ControllerRateConstraintCheckerTest {
  @Autowired ControllerRateConstraintChecker rateConstraintChecker;
  @Autowired UserRepository userRepository;

  class TestMethod {
    @RateLimit(target = LimitTarget.USER, key = "limitByUser", limit = 3, period = 30)
    public void limitByUser() {}

    @RateLimit(target = LimitTarget.IP, key = "limitByIp", limit = 3, period = 30)
    public void limitByIp() {}
  }

  TestMethod testMethod;

  @BeforeEach
  void setup() {
    testMethod = new TestMethod();
  }

  @Test
  @Transactional
  void checkWithMethodLimitByUser() throws Exception {
    User testUser = new User();
    testUser.setUserName("testUser");
    testUser.setRole(Role.NORMAL);
    testUser.setEmail("testUser@email.com");
    testUser.setPassword("pa55ward");
    userRepository.save(testUser);
    // mock login
    MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
    mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
    mockedStatic.when(AuthUtil::currentUserDetail).thenReturn(new UserDetail(testUser));
    // test for normal request
    assertDoesNotThrow(
        () ->
            rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByUser")));
    assertDoesNotThrow(
        () ->
            rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByUser")));
    assertDoesNotThrow(
        () ->
            rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByUser")));
    // test when exceed rate limit
    assertThrows(
        ControllerConstraintViolation.class,
        () ->
            rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByUser")));
    // test token refill
    Thread.sleep(10000);
    assertDoesNotThrow(
        () ->
            rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByUser")));
    // clear mock login
    mockedStatic.close();
  }

  @Test
  void checkWithMethodLimitByIp() throws Exception {
    // mock ip
    MockedStatic<IPUtils> mockedStatic = Mockito.mockStatic(IPUtils.class);
    mockedStatic.when(IPUtils::getRequestIP).thenReturn("127.0.0.1");
    // test for normal request
    assertDoesNotThrow(
        () -> rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByIp")));
    assertDoesNotThrow(
        () -> rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByIp")));
    assertDoesNotThrow(
        () -> rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByIp")));
    // test when exceed rate limit
    assertThrows(
        ControllerConstraintViolation.class,
        () -> rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByIp")));
    // test token refill
    Thread.sleep(10000);
    assertDoesNotThrow(
        () -> rateConstraintChecker.checkWithMethod(testMethod.getClass().getMethod("limitByIp")));
    // clear mock
    mockedStatic.close();
  }
}
