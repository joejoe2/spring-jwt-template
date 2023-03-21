package com.joejoe2.demo.controller;

import com.joejoe2.demo.controller.constraint.auth.AuthenticatedApi;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.service.user.profile.ProfileService;
import com.joejoe2.demo.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(path = "/api/user") // path prefix
public class UserController {
  @Autowired ProfileService profileService;

  @Operation(
      summary = "get profile of login user",
      description = "this is allowed to any authenticated user")
  @AuthenticatedApi
  @SecurityRequirement(name = "jwt")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "user profile",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfile.class))),
      })
  @RequestMapping(path = "/profile", method = RequestMethod.GET)
  public ResponseEntity profile() {
    Map<String, Object> response = new HashMap<>();
    try {
      return ResponseEntity.ok(profileService.getProfile(AuthUtil.currentUserDetail().getId()));
    } catch (UserDoesNotExist ex) {
      // will occur if user is not in db but the userDetail is loaded before this method
      // with JwtAuthenticationFilter, so only the db corrupt will cause this
      response.put("message", "unknown error, please try again later !");
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
