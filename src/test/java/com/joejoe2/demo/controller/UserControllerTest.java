package com.joejoe2.demo.controller;

import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.UserRepository;
import com.joejoe2.demo.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {
    @MockBean
    UserService userService;

    @Autowired
    UserRepository userRepository;

    User user;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void createUser(){
        user=new User();
        user.setUserName("testUser");
        user.setRole(Role.NORMAL);
        user.setEmail("testAdmin@email.com");
        user.setPassword("pa55ward");
        userRepository.save(user);
    }

    @AfterEach
    void deleteUser(){
        userRepository.delete(user);
    }

    @Test
    @WithUserDetails(value = "testUser", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void profile() throws Exception{
        User user=new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setCreateAt(LocalDateTime.now());
        user.setEmail("test@email.com");
        //test success
        Mockito.when(userService.getProfile(Mockito.any())).thenReturn(new UserProfile(user));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/profile")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.profile.username").value(user.getUserName()))
                .andExpect(jsonPath("$.profile.email").value(user.getEmail()))
                .andExpect(jsonPath("$.profile.role").value(user.getRole().toString()))
                .andExpect(jsonPath("$.profile.isActive").value(user.isActive()))
                .andExpect(jsonPath("$.profile.registeredAt").value(user.getCreateAt().toString()));
        //test 500
        Mockito.when(userService.getProfile(Mockito.any())).thenThrow(new UserDoesNotExist(""));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/profile")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}