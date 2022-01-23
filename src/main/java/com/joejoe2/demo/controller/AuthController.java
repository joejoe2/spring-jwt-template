package com.joejoe2.demo.controller;

import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.request.LoginRequest;
import com.joejoe2.demo.data.auth.request.RefreshRequest;
import com.joejoe2.demo.data.auth.request.RegisterRequest;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.Role;
import com.joejoe2.demo.model.User;
import com.joejoe2.demo.service.JwtService;
import com.joejoe2.demo.service.UserService;
import com.joejoe2.demo.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/api/auth") //path prefix
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserService userService;

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest){
        Map<String, String> response = new HashMap<>();
        try {
            UserDetail userDetail = AuthUtil.authenticate(authenticationManager, loginRequest.getUsername(), loginRequest.getPassword());
            TokenPair tokenPair = jwtService.issueTokens(userDetail);
            response.put("access_token", tokenPair.getAccessToken().getToken());
            response.put("refresh_token", tokenPair.getRefreshToken().getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AuthenticationException|InvalidOperation e){
            response.put("info", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/refresh", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshRequest refreshRequest){
        Map<String, String> response = new HashMap<>();
        try {
            TokenPair tokenPair = jwtService.refreshTokens(refreshRequest.getToken());
            response.put("access_token", tokenPair.getAccessToken().getToken());
            response.put("refresh_token",  tokenPair.getRefreshToken().getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidTokenException e) {
            response.put("info", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> logout(){
        Map<String, String> response = new HashMap<>();
        try {
            jwtService.revokeAccessToken(AuthUtil.currentUserDetail().getCurrentAccessToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidTokenException e) {
            response.put("info", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest registerRequest){
        Map<String, String> response = new HashMap<>();
        try {
            User user = userService.createUser(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail(), Role.NORMAL);
            response.put("id", user.getId().toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ValidationError | AlreadyExist e) {
            response.put("info", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
