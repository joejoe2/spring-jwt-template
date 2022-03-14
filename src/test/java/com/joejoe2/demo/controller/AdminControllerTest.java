package com.joejoe2.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("admin")
class AdminControllerTest {
    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper=new ObjectMapper();

    @Test
    void changeRole() throws Exception{
        //test success
        Mockito.doNothing().when(userService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+UUID.randomUUID()+"\", \"role\": \"ADMIN\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //test 400
        //0. validation
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"invalid id\", \"role\": \"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.errors.id").exists())
                        .andExpect(jsonPath("$.errors.role").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidOperation
        Mockito.doThrow(new InvalidOperation("")).when(userService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+UUID.randomUUID()+"\", \"role\": \"ADMIN\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //2. UserDoesNotExist
        Mockito.doThrow(new UserDoesNotExist("")).when(userService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+UUID.randomUUID()+"\", \"role\": \"ADMIN\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //3. role is not exist
        Mockito.doNothing().when(userService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+UUID.randomUUID()+"\", \"role\": \"xxx\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("role is not exist !"));
    }

    @Test
    void activateUser() throws Exception{
        String id = UUID.randomUUID().toString();
        //test success
        Mockito.doNothing().when(userService).activateUser(id);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+id+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //test 400
        //0. validation
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"invalid id\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.id").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidOperation
        Mockito.doThrow(new InvalidOperation("")).when(userService).activateUser(id);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+id+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //2. UserDoesNotExist
        Mockito.doThrow(new UserDoesNotExist("")).when(userService).activateUser(id);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+id+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateUser() throws Exception{
        String id = UUID.randomUUID().toString();
        //test success
        Mockito.doNothing().when(userService).deactivateUser(id);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+id+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //test 400
        //0. validation
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"invalid id\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.id").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidOperation
        Mockito.doThrow(new InvalidOperation("")).when(userService).deactivateUser(id);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+id+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //2. UserDoesNotExist
        Mockito.doThrow(new UserDoesNotExist("")).when(userService).deactivateUser(id);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\""+id+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    static class CustomResponse{
        public List profiles;

        CustomResponse(List profiles){
            this.profiles=profiles;
        }
    }

    @Test
    void getAllUserProfiles() throws Exception{
        List<UserProfile> profiles=new ArrayList<>();
        for (int i=0;i<50;i++){
            User user=new User();
            user.setId(UUID.randomUUID());
            user.setUserName("test"+i);
            user.setEmail("test"+i+"@email.com");
            user.setRole(i%3==0? Role.ADMIN:i%3==1?Role.STAFF:Role.NORMAL);
            user.setActive(i % 2 == 0);
            user.setCreateAt(LocalDateTime.now());
            profiles.add(new UserProfile(user));
        }
        //test success
        Mockito.doReturn(profiles).when(userService).getAllUserProfiles();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/getUserList")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profiles").isArray())
                .andExpect(status().isOk()).andReturn();
        assertEquals(objectMapper.writeValueAsString(new CustomResponse(profiles)), result.getResponse().getContentAsString());
        //test success with page request
        int page=2, size=10;
        Mockito.doReturn(new PageList<>(10, 2, 5, 10, profiles.subList(20, 30))).when(userService).getAllUserProfilesWithPage(page, size);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/getUserList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":\""+page+"\", \"size\":\""+size+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profiles").isArray())
                .andExpect(jsonPath("$.totalItems").value(10))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(status().isOk()).andReturn();
        //test 400
        //0. validation
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/getUserList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":\""+-1+"\", \"size\":\""+0+"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.page").exists())
                .andExpect(jsonPath("$.errors.size").exists())
                .andExpect(status().isBadRequest());
    }
}