package com.joejoe2.demo.controller;

import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.service.UserService;
import com.joejoe2.demo.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/api/user") //path prefix
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping(path = "/profile", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> profile(){
        Map<String, Object> response = new HashMap<>();
        try{
            response.put("profile", userService.getProfile(AuthUtil.currentUserDetail()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (InvalidOperation ex){
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
