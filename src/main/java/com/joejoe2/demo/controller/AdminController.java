package com.joejoe2.demo.controller;

import com.joejoe2.demo.controller.constraint.auth.ApiAllowsTo;
import com.joejoe2.demo.data.ErrorMessageResponse;
import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.PageOfUserProfile;
import com.joejoe2.demo.data.PageRequest;
import com.joejoe2.demo.data.admin.request.ChangeUserRoleRequest;
import com.joejoe2.demo.data.admin.request.UserIdRequest;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.service.user.auth.ActivationService;
import com.joejoe2.demo.service.user.auth.RoleService;
import com.joejoe2.demo.service.user.profile.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import javax.validation.Valid;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(path = "/api/admin") // path prefix
public class AdminController {
  @Autowired RoleService roleService;
  @Autowired ActivationService activationService;
  @Autowired ProfileService profileService;

  @Operation(
      summary = "change the role of target user",
      description = "this is only allowed to ADMIN")
  @ApiAllowsTo(roles = Role.ADMIN)
  @SecurityRequirements({
    @SecurityRequirement(name = "jwt"),
    @SecurityRequirement(name = "jwt-in-cookie")
  })
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description =
                """
                    <ul>
                    <li>you are not ADMIN</li>
                    <li>target user==request user</li>
                    <li>target role==original role</li>
                    <li>target user is the only ADMIN</li>
                    </ul>""",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "target user is not exist",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description =
                "after success, target user will be logged out "
                    + "by revoke all access and refresh tokens",
            content = @Content),
      })
  @RequestMapping(path = "/changeRoleOf", method = RequestMethod.POST)
  public ResponseEntity changeRole(@Valid @RequestBody ChangeUserRoleRequest request) {
    try {
      roleService.changeRoleOf(request.getId(), Role.valueOf(request.getRole()));
      return ResponseEntity.ok().build();
    } catch (InvalidOperation e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    } catch (UserDoesNotExist e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "activate target user", description = "this is only allowed to ADMIN")
  @ApiAllowsTo(roles = Role.ADMIN)
  @SecurityRequirements({
    @SecurityRequirement(name = "jwt"),
    @SecurityRequirement(name = "jwt-in-cookie")
  })
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description =
                """
                    <ul>
                    <li>you are not ADMIN</li>
                    <li>target user==request user</li>
                    <li>target user is already active</li>
                    </ul>""",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "target user is not exist",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description =
                "after success, target user will be logged out "
                    + "by revoke all access and refresh tokens",
            content = @Content),
      })
  @RequestMapping(path = "/activateUser", method = RequestMethod.POST)
  public ResponseEntity activateUser(@Valid @RequestBody UserIdRequest request) {
    try {
      activationService.activateUser(request.getId());
      return ResponseEntity.ok().build();
    } catch (InvalidOperation e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    } catch (UserDoesNotExist e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "deactivate target user", description = "this is only allowed to ADMIN")
  @ApiAllowsTo(roles = Role.ADMIN)
  @SecurityRequirements({
    @SecurityRequirement(name = "jwt"),
    @SecurityRequirement(name = "jwt-in-cookie")
  })
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description =
                """
                    <ul>
                    <li>you are not ADMIN</li>
                    <li>target user==request user</li>
                    <li>target user is already inactive</li>
                    </ul>""",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "target user is not exist",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description =
                "after success, target user will be logged out "
                    + "by revoke all access and refresh tokens",
            content = @Content),
      })
  @RequestMapping(path = "/deactivateUser", method = RequestMethod.POST)
  public ResponseEntity deactivateUser(@Valid @RequestBody UserIdRequest request) {
    try {
      activationService.deactivateUser(request.getId());
      return ResponseEntity.ok().build();
    } catch (InvalidOperation e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    } catch (UserDoesNotExist e) {
      return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }
  }

  @Operation(
      summary = "get all user profiles with page param",
      description = "this is only allowed to ADMIN")
  @ApiAllowsTo(roles = Role.ADMIN)
  @SecurityRequirements({
    @SecurityRequirement(name = "jwt"),
    @SecurityRequirement(name = "jwt-in-cookie")
  })
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "403",
            description =
                """
                    <ul>
                    <li>you are not ADMIN</li>
                    </ul>""",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(
            responseCode = "200",
            description = "get all user profiles with page param",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageOfUserProfile.class)))
      })
  @RequestMapping(path = "/getUserList", method = RequestMethod.GET)
  public ResponseEntity getAllUserProfiles(@ParameterObject @Valid PageRequest request) {
    PageList<UserProfile> pageList =
        profileService.getAllUserProfilesWithPage(request.getPage(), request.getSize());
    return ResponseEntity.ok(new PageOfUserProfile(pageList));
  }
}
