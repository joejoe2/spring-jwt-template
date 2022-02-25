package com.joejoe2.demo.controller;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.config.ResetPasswordURL;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.data.auth.request.*;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import com.joejoe2.demo.service.EmailService;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    JwtConfig jwtConfig;
    @Autowired
    UserService userService;
    @Autowired
    VerificationService verificationService;
    @Autowired
    EmailService emailService;
    @Autowired
    ResetPasswordURL resetPasswordURL;

    @RequestMapping(path = "/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            UserDetail userDetail = AuthUtil.authenticate(authenticationManager, request.getUsername(), request.getPassword());
            TokenPair tokenPair = jwtService.issueTokens(userDetail);
            response.put("access_token", tokenPair.getAccessToken().getToken());
            response.put("refresh_token", tokenPair.getRefreshToken().getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AuthenticationException e){
            response.put("message", e.getMessage()+" !");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (InvalidOperation e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/web/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> webLogin(@Valid @RequestBody LoginRequest request, HttpServletResponse response){
        Map<String, String> responseBody = new HashMap<>();
        try {
            UserDetail userDetail = AuthUtil.authenticate(authenticationManager, request.getUsername(), request.getPassword());
            TokenPair tokenPair = jwtService.issueTokens(userDetail);
            responseBody.put("access_token", tokenPair.getAccessToken().getToken());
            //place refresh_token in http only cookie
            Cookie cookie = new Cookie("refresh_token", tokenPair.getRefreshToken().getToken());
            cookie.setMaxAge(jwtConfig.getRefreshTokenLifetimeSec());
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (AuthenticationException e){
            responseBody.put("message", e.getMessage()+" !");
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }catch (InvalidOperation e){
            responseBody.put("message", e.getMessage());
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/api/auth/refresh", method = RequestMethod.POST)
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

    @RequestMapping(path = "/web/api/auth/refresh", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> webRefresh(@CookieValue(name = "refresh_token", defaultValue = "") String refreshToken, HttpServletResponse response){
        Map<String, String> responseBody = new HashMap<>();
        try {
            TokenPair tokenPair = jwtService.refreshTokens(refreshToken);
            responseBody.put("access_token", tokenPair.getAccessToken().getToken());
            //place refresh_token in http only cookie
            Cookie cookie = new Cookie("refresh_token", tokenPair.getRefreshToken().getToken());
            cookie.setMaxAge(jwtConfig.getRefreshTokenLifetimeSec());
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (InvalidTokenException e) {
            responseBody.put("message", e.getMessage());
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/api/auth/logout", method = RequestMethod.POST)
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

    @RequestMapping(path = "/api/auth/register", method = RequestMethod.POST)
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

    @RequestMapping(path = "/api/auth/issueVerificationCode", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> issueVerificationCode(@Valid @RequestBody IssueVerificationCodeRequest request){
        Map<String, String> response = new HashMap<>();
        VerificationPair verificationPair = verificationService.issueVerificationCode(request.getEmail());
        response.put("key", verificationPair.getKey());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(path = "/api/auth/changePassword", method = RequestMethod.POST)
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


    @RequestMapping(path = "/api/auth/forgetPassword", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            VerifyToken verifyToken=userService.requestResetPassword(request.getEmail());
            //send reset password link to user
            emailService.sendSimpleEmail(request.getEmail(), "Your Reset Password Link",
                    "click the link to reset your password:\n" + resetPasswordURL.getUrlPrefix()+verifyToken.getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (InvalidOperation ex){
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/api/auth/resetPassword", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (InvalidOperation ex){
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
