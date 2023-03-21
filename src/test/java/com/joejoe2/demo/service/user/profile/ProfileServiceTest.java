package com.joejoe2.demo.service.user.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class ProfileServiceTest {
  @Autowired ProfileService profileService;
  @Autowired UserRepository userRepository;

  @Test
  @Transactional
  void getProfileWithIllegalArgumentException() {
    // test IllegalArgument
    assertThrows(IllegalArgumentException.class, () -> profileService.getProfile("invalid uid"));
  }

  @Test
  @Transactional
  void getProfileWith() {
    User user = new User();
    user.setUserName("test");
    user.setEmail("test@email.com");
    user.setPassword("pa55ward");
    user.setRole(Role.NORMAL);
    userRepository.save(user);
    userRepository.deleteById(user.getId());
    // test with a not exist user
    assertThrows(UserDoesNotExist.class, () -> profileService.getProfile(user.getId().toString()));
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
    userRepository.flush();
    UserProfile profile;
    // test success
    try {
      profile = profileService.getProfile(user.getId().toString());
    } catch (Exception e) {
      throw new AssertionError(e);
    }
    assertEquals(new UserProfile(user), profile);
  }

  @Test
  @Transactional
  void getAllUserProfiles() {
    Random random = new Random();
    long count = userRepository.count();
    long r = random.nextInt(100);
    for (int i = 0; i < r; i++) {
      User user = new User();
      user.setUserName("test" + i);
      user.setPassword("pa55ward");
      user.setEmail("test" + i + "@email.com");
      userRepository.save(user);
    }
    assertEquals(count + r, profileService.getAllUserProfiles().size());
  }

  @Test
  @Transactional
  void getAllUserProfilesWithIllegalPage() {
    // test IllegalArgument
    assertThrows(
        IllegalArgumentException.class, () -> profileService.getAllUserProfilesWithPage(-1, 1));
    assertThrows(
        IllegalArgumentException.class, () -> profileService.getAllUserProfilesWithPage(0, -1));
    assertThrows(
        IllegalArgumentException.class, () -> profileService.getAllUserProfilesWithPage(0, 0));
  }

  @Test
  @Transactional
  void getAllUserProfilesWithPage() {
    // test success
    PageList<UserProfile> pageList;
    try {
      pageList = profileService.getAllUserProfilesWithPage(5, 10);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
    assertEquals(5, pageList.getCurrentPage());
    assertEquals(10, pageList.getPageSize());
  }
}
