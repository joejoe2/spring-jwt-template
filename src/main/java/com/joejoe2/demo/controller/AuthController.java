package com.joejoe2.demo.controller;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.config.ResetPasswordURL;
import com.joejoe2.demo.controller.constraint.auth.AuthenticatedApi;
import com.joejoe2.demo.controller.constraint.rate.LimitTarget;
import com.joejoe2.demo.controller.constraint.rate.RateLimit;
import com.joejoe2.demo.data.ErrorMessageResponse;
import com.joejoe2.demo.data.auth.*;
import com.joejoe2.demo.data.auth.request.*;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import com.joejoe2.demo.service.email.EmailService;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.service.user.auth.LoginService;
import com.joejoe2.demo.service.user.auth.PasswordService;
import com.joejoe2.demo.service.user.auth.RegistrationService;
import com.joejoe2.demo.service.verification.VerificationService;
import com.joejoe2.demo.utils.AuthUtil;
import com.joejoe2.demo.utils.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthController {
  @Autowired LoginService loginService;
  @Autowired JwtService jwtService;
  @Autowired JwtConfig jwtConfig;
  @Autowired RegistrationService registrationService;
  @Autowired PasswordService passwordService;

  @Autowired VerificationService verificationService;
  @Autowired EmailService emailService;
  @Autowired ResetPasswordURL resetPasswordURL;

  @Operation(
      summary = "login and get the jwt access and refresh tokens",
      description = "this is allowed to everyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description =
                """
                    <ul>
                    <li>user is not exist</li>
                    <li>user is inactive</li>
                    <li>incorrect username or password</li>
                    </ul>""",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "login and get the jwt access and refresh tokens",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenResponse.class)))
      })
  @RequestMapping(path = "/api/auth/login", method = RequestMethod.POST)
  public ResponseEntity login(@Valid @RequestBody LoginRequest request) {
    try {
      UserDetail userDetail = loginService.login(request.getUsername(), request.getPassword());
      TokenPair tokenPair = jwtService.issueTokens(userDetail);
      return ResponseEntity.ok(new TokenResponse(tokenPair));
    } catch (AuthenticationException e) {
      return new ResponseEntity<>(
          new ErrorMessageResponse(e.getMessage() + " !"), HttpStatus.FORBIDDEN);
    } catch (UserDoesNotExist e) {
      // if user is deleted after AuthUtil.authenticate and before jwtService.issueTokens
      // this is considered to be an accident
      return new ResponseEntity<>(
          new ErrorMessageResponse("unknown error, please try again later !"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(
      summary = "login and get the access/refresh tokens in http-only cookie",
      description = "this is allowed to everyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description =
                """
                    <ul>
                    <li>user is not exist</li>
                    <li>user is inactive</li>
                    <li>incorrect username or password</li>
                    </ul>""",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "get access/refresh tokens in http-only cookie",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)),
            headers = {
              @Header(name = "access_token", description = "access token in http-only cookie"),
              @Header(name = "refresh_token", description = "refresh token in http-only cookie")
            })
      })
  @RequestMapping(path = "/web/api/auth/login", method = RequestMethod.POST)
  public ResponseEntity webLogin(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    try {
      UserDetail userDetail = loginService.login(request.getUsername(), request.getPassword());
      TokenPair tokenPair = jwtService.issueTokens(userDetail);
      // place refresh token in http only cookie
      response.addCookie(
          CookieUtils.create(
              "refresh_token",
              tokenPair.getRefreshToken().getToken(),
              jwtConfig.getCookieDomain(),
              jwtConfig.getRefreshTokenLifetimeSec(),
              true));
      response.addCookie(
          CookieUtils.create(
              "access_token",
              tokenPair.getAccessToken().getToken(),
              jwtConfig.getCookieDomain(),
              jwtConfig.getAccessTokenLifetimeSec(),
              true));
      return new ResponseEntity<>(
          Collections.singletonMap("access_token", tokenPair.getAccessToken().getToken()),
          HttpStatus.OK);
    } catch (AuthenticationException e) {
      return new ResponseEntity<>(
          new ErrorMessageResponse(e.getMessage() + " !"), HttpStatus.FORBIDDEN);
    } catch (UserDoesNotExist e) {
      // if user is deleted after AuthUtil.authenticate and before jwtService.issueTokens
      // this is considered to be an accident
      return new ResponseEntity<>(
          new ErrorMessageResponse("unknown error, please try again later !"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(
      summary = "use refresh token to exchange new access and refresh tokens",
      description = "this is allowed to everyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description = "the refresh token is invalid (could be expired or revoked)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "exchange new access and refresh tokens",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenResponse.class)))
      })
  @RequestMapping(path = "/api/auth/refresh", method = RequestMethod.POST)
  public ResponseEntity refresh(@Valid @RequestBody RefreshRequest request) {
    try {
      TokenPair tokenPair = jwtService.refreshTokens(request.getToken());
      return ResponseEntity.ok(new TokenResponse(tokenPair));
    } catch (InvalidTokenException | InvalidOperation e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }
  }

  @Operation(
      summary =
          "use refresh token(in your http-only cookie) to exchange new access/refresh tokens in"
              + " http-only cookie",
      description =
          "this is allowed to everyone but protected by the <code>allow.host</code> cors"
              + " setting")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description = "the refresh token is invalid (could be expired or revoked)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "exchange new access/refresh tokens",
            content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)),
            headers = {
              @Header(name = "access_token", description = "access token in http-only cookie"),
              @Header(name = "refresh_token", description = "refresh token in http-only cookie")
            })
      })
  @RequestMapping(path = "/web/api/auth/refresh", method = RequestMethod.POST)
  public ResponseEntity<Map<String, String>> webRefresh(
      @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken,
      HttpServletResponse response) {
    Map<String, String> responseBody = new HashMap<>();
    try {
      TokenPair tokenPair = jwtService.refreshTokens(refreshToken);
      responseBody.put("access_token", tokenPair.getAccessToken().getToken());
      // place tokens in http only cookie
      response.addCookie(
          CookieUtils.create(
              "refresh_token",
              tokenPair.getRefreshToken().getToken(),
              jwtConfig.getCookieDomain(),
              jwtConfig.getRefreshTokenLifetimeSec(),
              true));
      response.addCookie(
          CookieUtils.create(
              "access_token",
              tokenPair.getAccessToken().getToken(),
              jwtConfig.getCookieDomain(),
              jwtConfig.getAccessTokenLifetimeSec(),
              true));
      return new ResponseEntity<>(responseBody, HttpStatus.OK);
    } catch (InvalidTokenException | InvalidOperation e) {
      responseBody.put("message", e.getMessage());
      return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
    }
  }

  @Operation(
      summary = "use access token to logout related user",
      description = "this is allowed to any authenticated user")
  @AuthenticatedApi
  @SecurityRequirements({
    @SecurityRequirement(name = "jwt"),
    @SecurityRequirement(name = "jwt-in-cookie")
  })
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "logout", content = @Content),
      })
  @RequestMapping(path = "/api/auth/logout", method = RequestMethod.POST)
  public ResponseEntity logout(HttpServletResponse response) {
    try {
      jwtService.revokeAccessToken(AuthUtil.currentUserDetail().getCurrentAccessTokenID());
      // clear tokens in cookie
      response.addCookie(CookieUtils.removed("access_token", jwtConfig.getCookieDomain(), true));
      response.addCookie(CookieUtils.removed("refresh_token", jwtConfig.getCookieDomain(), true));
      return ResponseEntity.ok().build();
    } catch (InvalidTokenException e) {
      // because the access token has been checked by JwtAuthenticationFilter,
      // this will happen if the access token is deleted/revoked by another request,
      // or even the access token go expired between JwtAuthenticationFilter and jwtService,
      // so we just return 401 to represent that the access token is invalid
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  @Operation(summary = "decode and check access token", description = "this is allowed to anyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "400",
            description = "invalid access token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "decoded access token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccessTokenSpec.class)))
      })
  @RequestMapping(path = "/api/auth/introspect", method = RequestMethod.POST)
  public ResponseEntity introspect(@Valid @RequestBody IntrospectionRequest request) {
    try {
      return new ResponseEntity<>(jwtService.introspect(request.getToken()), HttpStatus.OK);
    } catch (InvalidTokenException e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @Operation(summary = "register an user", description = "this is allowed to everyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description = "registration fail",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "register an user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfile.class)))
      })
  @RequestMapping(path = "/api/auth/register", method = RequestMethod.POST)
  public ResponseEntity register(@Valid @RequestBody RegisterRequest request) {
    Map<String, String> response = new HashMap<>();
    try {
      User user =
          registrationService.registerUser(
              request.getUsername(),
              request.getPassword(),
              request.getEmail(),
              request.getVerification());
      return ResponseEntity.ok(new UserProfile(user));
    } catch (AlreadyExist | InvalidOperation e) {
      response.put("message", e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
  }

  @Operation(
      summary = "issue a verification code with email",
      description = "this is allowed to everyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "send a verification code to the email, note that the \"key\" is"
                    + " used along with verification code to pass the verification",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VerificationKey.class)))
      })
  @RequestMapping(path = "/api/auth/issueVerificationCode", method = RequestMethod.POST)
  public ResponseEntity issueVerificationCode(
      @Valid @RequestBody IssueVerificationCodeRequest request) {
    VerificationPair verificationPair =
        verificationService.issueVerificationCode(request.getEmail());
    return ResponseEntity.ok(new VerificationKey(verificationPair.getKey()));
  }

  @Operation(
      summary = "change your password to new password",
      description =
          "this is allowed to any authenticated user "
              + "and having a rate limit(10 times / 3600 sec) for each user")
  @AuthenticatedApi
  @RateLimit(target = LimitTarget.USER, key = "/api/auth/changePassword", limit = 10, period = 3600)
  @SecurityRequirements({
    @SecurityRequirement(name = "jwt"),
    @SecurityRequirement(name = "jwt-in-cookie")
  })
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description = "password change fail",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description =
                "after success, you will be logged out "
                    + "by revoke all access and refresh tokens",
            content = @Content)
      })
  @RequestMapping(path = "/api/auth/changePassword", method = RequestMethod.POST)
  public ResponseEntity changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    try {
      passwordService.changePasswordOf(
          AuthUtil.currentUserDetail().getId(), request.getOldPassword(), request.getNewPassword());
      return ResponseEntity.ok().build();
    } catch (InvalidOperation e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    } catch (UserDoesNotExist e) {
      // will occur if user is not in db but the userDetail is loaded before this method
      // with JwtAuthenticationFilter
      // this is considered to be an accident
      return new ResponseEntity<>(
          new ErrorMessageResponse("unknown error, please try again later !"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(
      summary = "request a password reset link and send to your email",
      description = "this is allowed to everyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description = "fail to generate a password reset link",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "send a password reset link to your email",
            content = @Content)
      })
  @RequestMapping(path = "/api/auth/forgetPassword", method = RequestMethod.POST)
  public ResponseEntity forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
    try {
      VerifyToken verifyToken = passwordService.requestResetPasswordToken(request.getEmail());
      emailService.sendSimpleEmail(
          request.getEmail(),
          "Your Reset Password Link",
          "click the link to reset your password:\n"
              + resetPasswordURL.getUrlPrefix()
              + verifyToken.getToken());
      return ResponseEntity.ok().build();
    } catch (UserDoesNotExist | InvalidOperation e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }
  }

  @Operation(
      summary = "use token after password reset link to reset password",
      description = "this is allowed to everyone")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description = "fail to reset password",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description =
                "after success, you will be logged out "
                    + "by revoke all access and refresh tokens",
            content = @Content)
      })
  @RequestMapping(path = "/api/auth/resetPassword", method = RequestMethod.POST)
  public ResponseEntity resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    try {
      passwordService.resetPassword(request.getToken(), request.getNewPassword());
      return ResponseEntity.ok().build();
    } catch (InvalidOperation e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }
  }
}
