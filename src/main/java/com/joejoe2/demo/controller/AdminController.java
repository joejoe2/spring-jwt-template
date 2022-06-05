package com.joejoe2.demo.controller;

import com.joejoe2.demo.controller.constraint.auth.ApiAllowsTo;
import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.admin.request.ChangeUserRoleRequest;
import com.joejoe2.demo.data.PageRequest;
import com.joejoe2.demo.data.admin.request.UserIdRequest;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.service.user.auth.ActivationService;
import com.joejoe2.demo.service.user.auth.RoleService;
import com.joejoe2.demo.service.user.profile.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/api/admin") //path prefix
public class AdminController {
    @Autowired
    RoleService roleService;
    @Autowired
    ActivationService activationService;
    @Autowired
    ProfileService profileService;

    /**
     * change the role of target user, this is only allowed to ADMIN <br><br>
     * 1. will return code 401 if you are not authenticated
     * (the access token is invalid, expired, or revoked)<br><br>
     *
     * 2. will return code 403 and {"message": "xxx"} if
     *    <ul>
     *        <li>you are not ADMIN</li>
     *        <li>target user==request user</li>
     *        <li>target role==original role</li>
     *        <li>target user is the only ADMIN</li>
     *    </ul><br><br>
     *
     * 3. will return code 404 if target user is not exist<br><br>
     *
     * 4. any {@link ChangeUserRoleRequest} with invalid body will return code 400 and
     *    <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     *
     * 5. if the {@link ChangeUserRoleRequest} success,
     *    target user will be logged out by revoke all access and refresh tokens</li>
     *
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>401</li>
     *     <li>403, <code>{"message": "xxx"}</code></li>
     *     <li>404, <code>{"message": "xxx"}</code></li>
     * </ul>
     */
    @ApiAllowsTo(roles = Role.ADMIN)
    @RequestMapping(path = "/changeRoleOf", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> changeRole(@Valid @RequestBody ChangeUserRoleRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            roleService.changeRoleOf(request.getId(), Role.valueOf(request.getRole()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidOperation e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * activate target user, this is only allowed to ADMIN <br><br>
     * 1. will return code 401 if you are not authenticated
     * (the access token is invalid, expired, or revoked)<br><br>
     *
     * 2. will return code 403 if
     *      <ul>
     *        <li>target user==request user</li>
     *        <li>target user is already active</li>
     *    </ul><br><br>
     *
     * 3. will return code 404 if target user is not exist<br><br>
     *
     * 4. any {@linkplain UserIdRequest ActivateUserRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages
     *
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>400, <code>{"message": "xxx"}</code></li>
     *     <li>401</li>
     *     <li>403, <code>{"message": "xxx"}</code></li>
     *     <li>404, <code>{"message": "xxx"}</code></li>
     * </ul>
     */
    @ApiAllowsTo(roles = Role.ADMIN)
    @RequestMapping(path = "/activateUser", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> activateUser(@Valid @RequestBody UserIdRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            activationService.activateUser(request.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidOperation e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * deactivate target user(cannot login anymore), this is only allowed to ADMIN <br><br>
     * 1. will return code 401 if you are not authenticated
     * (the access token is invalid, expired, or revoked)<br><br>
     *
     * 2. will return code 403 if
     *      <ul>
     *        <li>target user==request user</li>
     *        <li>target user is already inactive</li>
     *      </ul><br><br>
     *
     * 3. any {@linkplain UserIdRequest DeActivateUserRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     *
     * 4. if the {@linkplain UserIdRequest DeActivateUserRequest} success
     *    ,target user will be logged out by revoke all access and refresh tokens
     *
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>400, <code>{"message": "xxx"}</code></li>
     *     <li>401</li>
     *     <li>403, <code>{"message": "xxx"}</code></li>
     *     <li>404, <code>{"message": "xxx"}</code></li>
     * </ul>
     */
    @ApiAllowsTo(roles = Role.ADMIN)
    @RequestMapping(path = "/deactivateUser", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> deactivateUser(@Valid @RequestBody UserIdRequest request){
        Map<String, String> response = new HashMap<>();
        try {
            activationService.deactivateUser(request.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidOperation e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e){
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * get all user profiles or user profiles with page param, this is only allowed to ADMIN <br><br>
     * 1. will return code 401 if you are not authenticated
     * (the access token is invalid, expired, or revoked)<br><br>
     *
     * 2. any {@linkplain PageRequest GetUserProfilesRequest} with invalid body will return code 400 and <code>{"errors": ["field name": ["error msg", ...], ...]}</code>
     *    to specify the fields failing to pass the validation with errors messages <br><br>
     *
     * 3. if no given {@linkplain PageRequest request body},
     *    this will return all user profiles, otherwise will be a page request
     * @param request
     * @return status code, json
     * <ul>
     *     <li>200, <code>{"profiles": [{"id":"xxx","username":"xxx","email":"xxx","role":"xxx","isActive":true or false,"registeredAt":"time in utc"}, ...]}</code></li>
     *     <li>200, <code>{"profiles": [{"id":"xxx","username":"xxx","email":"xxx","role":"xxx","isActive":true or false,"registeredAt":"time in utc"}, ...]
     *     , "totalItems": int, "currentPage": int,"totalPages": int, "pageSize": int}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>401</li>
     * </ul>
     */
    @ApiAllowsTo(roles = Role.ADMIN)
    @RequestMapping(path = "/getUserList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getAllUserProfiles(@Valid @RequestBody(required = false) PageRequest request){
        Map<String, Object> response = new HashMap<>();
        if (request==null)
            response.put("profiles", profileService.getAllUserProfiles());
        else {
            PageList<UserProfile> pageList = profileService.getAllUserProfilesWithPage(request.getPage(), request.getSize());
            response.put("profiles", pageList.getList());
            response.put("totalItems", pageList.getTotalItems());
            response.put("currentPage", pageList.getCurrentPage());
            response.put("totalPages", pageList.getTotalPages());
            response.put("pageSize", pageList.getPageSize());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
