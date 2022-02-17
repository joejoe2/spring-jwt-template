package com.joejoe2.demo.controller;

import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.data.auth.request.LoginRequest;
import com.joejoe2.demo.data.auth.request.RefreshRequest;
import com.joejoe2.demo.data.auth.request.RegisterRequest;
import com.joejoe2.demo.data.auth.request.IssueVerificationCodeRequest;
import com.joejoe2.demo.data.auth.request.ChangePasswordRequest;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.service.JwtService;
import com.joejoe2.demo.service.UserService;
import com.joejoe2.demo.service.VerificationService;
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

import javax.validation.Valid;
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
    @Autowired
    VerificationService verificationService;

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            UserDetail userDetail = AuthUtil.authenticate(authenticationManager, request.getUsername(), request.getPassword());
            TokenPair tokenPair = jwtService.issueTokens(userDetail);
            response.put("access_token", tokenPair.getAccessToken().getToken());
            response.put("refresh_token", tokenPair.getRefreshToken().getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AuthenticationException | InvalidOperation e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/refresh", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody RefreshRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            TokenPair tokenPair = jwtService.refreshTokens(request.getRefreshToken());
            response.put("access_token", tokenPair.getAccessToken().getToken());
            response.put("refresh_token",  tokenPair.getRefreshToken().getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidTokenException e) {
            response.put("message", e.getMessage());
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
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            User user = userService.registerUser(request.getUsername(), request.getPassword(), request.getEmail(), request.getVerification());
            response.put("id", user.getId().toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AlreadyExist | InvalidOperation e) {
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/issueVerificationCode", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> issueVerificationCode(@Valid @RequestBody IssueVerificationCodeRequest request){
        Map<String, String> response = new HashMap<>();
        VerificationPair verificationPair = verificationService.issueVerificationCode(request.getEmail());
        response.put("key", verificationPair.getKey());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            userService.changePasswordOf(AuthUtil.currentUserDetail().getId(), request.getOldPassword(), request.getNewPassword());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (InvalidOperation ex){
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
