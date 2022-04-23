package com.joejoe2.demo.controller;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.config.ResetPasswordURL;
import com.joejoe2.demo.controller.constraint.AuthenticatedApi;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.data.auth.request.*;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import com.joejoe2.demo.service.email.EmailService;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.service.user.auth.PasswordService;
import com.joejoe2.demo.service.user.auth.RegistrationService;
import com.joejoe2.demo.service.verification.VerificationService;
import com.joejoe2.demo.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    JwtConfig jwtConfig;
    @Autowired
    RegistrationService registrationService;
    @Autowired
    PasswordService passwordService;

    @Autowired
    VerificationService verificationService;
    @Autowired
    EmailService emailService;
    @Autowired
    ResetPasswordURL resetPasswordURL;

    /**
     * login and get the jwt access and refresh tokens, this is allowed to everyone <br><br>
     * 1. any {@link LoginRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * 2. a {@link LoginRequest} will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>user is not exist</li>
     *        <li>user is inactive</li>
     *        <li>incorrect username or password</li>
     *    </ul>
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{"access_token": "xxx", "refresh_token": "xxx"}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>400, <code>{"message": "xxx"}</code></li>
     * </ul>
     */
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
        }catch (UserDoesNotExist e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * login and get the jwt access tokens and set refresh in http-only cookie, this is allowed to everyone
     * but follow the <code>allow.host</code> setting <br><br>
     * 1. any {@link LoginRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * 2. a {@link LoginRequest} will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>user is not exist</li>
     *        <li>user is inactive</li>
     *        <li>incorrect username or password</li>
     *    </ul>
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{"access_token": "xxx"}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>400, <code>{"message": "xxx"}</code></li>
     * </ul>
     * and <code>"refresh_token"</code> will be set in your http-only cookie
     */
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
        }catch (UserDoesNotExist e){
            // if user is deleted after AuthUtil.authenticate and before jwtService.issueTokens
            // this is considered to be an accident
            responseBody.put("message", "unknown error, please try again later !");
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * use refresh token to exchange new access and refresh tokens, this is allowed to everyone<br><br>
     * 1. any {@link RefreshRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * 2. a {@link RefreshRequest} will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>the refresh token is invalid (could be expired or revoked)</li>
     *    </ul>
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{"access_token": "xxx", "refresh_token": "xxx"}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>400, <code>{"message": "xxx"}</code></li>
     * </ul>
     */
    @RequestMapping(path = "/api/auth/refresh", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody RefreshRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            TokenPair tokenPair = jwtService.refreshTokens(request.getRefresh_token());
            response.put("access_token", tokenPair.getAccessToken().getToken());
            response.put("refresh_token",  tokenPair.getRefreshToken().getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidTokenException e) {
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * use refresh token(in your http-only cookie) to exchange new access token and set new refresh in http-only cookie,
     * this is allowed to everyone but follow the <code>allow.host</code> setting <br><br>
     * 1. will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>you do not have "refresh_token" in your cookie</li>
     *        <li>the refresh token is invalid (could be expired or revoked)</li>
     *    </ul>
     * @param refreshToken in your http-only cookie (with key "refresh_token")
     * @return status code, json
     * <ul>
     *     <li>200, <code>{"access_token": "xxx"}</code></li>
     *     <li>400, <code>{"message": "xxx"}</code></li>
     * </ul>
     * and <code>"refresh_token"</code> will be set in your http-only cookie
     */
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

    /**
     * use access token to logout related user, this is allowed to any authenticated user(with access token),
     * notice that the access token and related refresh token will both be revoked after logout <br><br>
     * 1. will return code 401 if
     *    <ul>
     *        <li>the access token is invalid (could be expired or revoked)</li>
     *    </ul>
     * 2. will return code 403 if
     *    <ul>
     *        <li>you are not authenticated (no <code>AUTHORIZATION "Bearer access_token"</code> in header)</li>
     *    </ul>
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>401</li>
     *     <li>403</li>
     * </ul>
     */
    @AuthenticatedApi
    @RequestMapping(path = "/api/auth/logout", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> logout(){
        Map<String, String> response = new HashMap<>();
        try {
            jwtService.revokeAccessToken(AuthUtil.currentUserDetail().getCurrentAccessToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidTokenException e) {
            //because the access token has been checked by JwtAuthenticationFilter,
            //this will happen if the access token is deleted/revoked by another request,
            //or even the access token go expired between JwtAuthenticationFilter and jwtService,
            //so we just return 401 to represent that the access token is invalid
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * register a user with verification, this is allowed to everyone<br><br>
     * 1. any {@link RegisterRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * 2. a {@link RegisterRequest} will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>target user(username or email) is already taken</li>
     *        <li>you do not pass the verification via {@link VerificationService}</li>
     *    </ul>
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>400, <code>{"message": "xxx"}</code></li>
     * </ul>
     */
    @RequestMapping(path = "/api/auth/register", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            User user = registrationService.registerUser(request.getUsername(), request.getPassword(), request.getEmail(), request.getVerification());
            response.put("id", user.getId().toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AlreadyExist | InvalidOperation e) {
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * issue a verification code with email, this is allowed to everyone,
     * note that the "key" in response body is used along with verification code to pass the verification<br><br>
     * 1. any {@link IssueVerificationCodeRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{"key", "xxx"}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     * </ul>
     */
    @RequestMapping(path = "/api/auth/issueVerificationCode", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> issueVerificationCode(@Valid @RequestBody IssueVerificationCodeRequest request){
        Map<String, String> response = new HashMap<>();
        VerificationPair verificationPair = verificationService.issueVerificationCode(request.getEmail());
        response.put("key", verificationPair.getKey());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * change your password to new password, this is allowed to any authenticated user(with access token) <br><br>
     * 1. any {@link ChangePasswordRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * 2. a {@link ChangePasswordRequest} will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>old password is incorrect</li>
     *        <li>old password==new password</li>
     *    </ul>
     * 3. will return code 401 if
     *    <ul>
     *        <li>the access token is invalid (could be expired or revoked)</li>
     *    </ul>
     * 4. will return code 403 if
     *    <ul>
     *        <li>you are not authenticated (no <code>AUTHORIZATION "Bearer access_token"</code> in header)</li>
     *    </ul>
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>401</li>
     *     <li>403</li>
     * </ul>
     */
    @AuthenticatedApi
    @RequestMapping(path = "/api/auth/changePassword", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            passwordService.changePasswordOf(AuthUtil.currentUserDetail().getId(), request.getOldPassword(), request.getNewPassword());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (InvalidOperation ex){
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (UserDoesNotExist ex){
            // will occur if user is not in db but the userDetail is loaded before this method
            // with JwtAuthenticationFilter
            // this is considered to be an accident
            response.put("message", "unknown error, please try again later !");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * request a password reset link and send to your email, this is allowed to everyone <br><br>
     * 1. any {@link ForgetPasswordRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * 2. a {@link ForgetPasswordRequest} will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>target user (with that email) is not exist</li>
     *        <li>target user (with that email) is inactive</li>
     *        <li>there is a password reset link still active</li>
     *    </ul>
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     * </ul>
     */
    @RequestMapping(path = "/api/auth/forgetPassword", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            VerifyToken verifyToken=passwordService.requestResetPasswordToken(request.getEmail());
            //send reset password link to user
            emailService.sendSimpleEmail(request.getEmail(), "Your Reset Password Link",
                    "click the link to reset your password:\n" + resetPasswordURL.getUrlPrefix()+verifyToken.getToken());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (InvalidOperation | UserDoesNotExist ex){
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * use token after {@link AuthController#forgetPassword(ForgetPasswordRequest) password reset link} to reset password, this is allowed to everyone <br><br>
     * 1. any {@link ResetPasswordRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     * 2. a {@link ResetPasswordRequest} will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>target user is inactive</li>
     *        <li>the token from password reset link is not valid or expired</li>
     *    </ul>
     * @see AuthController#forgetPassword(ForgetPasswordRequest)
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     * </ul>
     */
    @RequestMapping(path = "/api/auth/resetPassword", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            passwordService.resetPassword(request.getToken(), request.getNewPassword());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (InvalidOperation ex){
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
