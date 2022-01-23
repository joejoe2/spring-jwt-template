package com.joejoe2.demo;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.Role;
import com.joejoe2.demo.model.User;
import com.joejoe2.demo.repository.UserRepository;
import com.joejoe2.demo.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class UserServiceTests {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Test
    public void testOptimisticLock() throws InterruptedException {
        Optional<User> userObj = userRepository.getByUserName("test");
        Assertions.assertTrue(userObj.isPresent());

        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        User user = userObj.get();
        Role target = Role.NORMAL;
        // transaction 1
        executorService.execute(() -> {
            try {
                userService.changeRoleOf(user.getId().toString(), target);
            } catch (InvalidOperation e) {
                e.printStackTrace();
            }
        });
        // transaction 2
        executorService.execute(() -> {
            try {
                userService.changeRoleOf(user.getId().toString(), target);
            } catch (InvalidOperation e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    };
}
