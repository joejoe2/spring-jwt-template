package com.joejoe2.demo.controller;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.admin.request.ChangeUserRoleRequest;
import com.joejoe2.demo.data.admin.request.PageRequest;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.model.Role;
import com.joejoe2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/api/admin") //path prefix
public class AdminController {
    @Autowired
    UserService userService;

    @RequestMapping(path = "/changeRole", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> changeRole(@RequestBody ChangeUserRoleRequest changeUserRoleRequest){
        Map<String, String> response = new HashMap<>();
        try {
            userService.changeRoleOf(changeUserRoleRequest.getId(), Role.valueOf(changeUserRoleRequest.getRole()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidOperation e){
            response.put("info", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e){
            response.put("info", "role is not exist !");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/getUserList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getAllUserProfiles(@RequestBody(required = false) PageRequest pageRequest){
        Map<String, Object> response = new HashMap<>();
        if (pageRequest==null)
            response.put("profiles", userService.getAllUserProfiles());
        else {
            try {
                PageList<UserProfile> pageList = userService.getAllUserProfilesWithPage(pageRequest.getPage(), pageRequest.getSize());
                response.put("profiles", pageList.getList());
                response.put("totalItems", pageList.getTotalItems());
                response.put("currentPage", pageList.getCurrentPage());
                response.put("totalPages", pageList.getTotalPages());
                response.put("pageSize", pageList.getPageSize());
            }catch (InvalidOperation e){
                response.put("info", e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
