package com.joejoe2.demo.controller;

import com.joejoe2.demo.data.auth.request.ChangePasswordRequest;
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

    /**
     * change your password to new password, this is allowed to any authenticated user(with access token) <br><br>
     * 1. a request will return code 400 and {"message": "xxx"} if
     *    <ul>
     *        <li>target user is not exist</li>
     *    </ul>
     * 2. will return code 401 if
     *    <ul>
     *        <li>the access token is invalid (could be expired or revoked)</li>
     *    </ul>
     * 3. will return code 403 if
     *    <ul>
     *        <li>you are not authenticated (no <code>AUTHORIZATION "Bearer access_token"</code> in header)</li>
     *    </ul>
     * @return status code, json
     * <ul>
     *     <li>200, <code>{"id": "xxx",
     *     "username": "xxx",
     *     "email": "xxx",
     *     "role": "xxx",
     *     "isActive": true or false,
     *     "registeredAt": "time in utc"}</code></li>
     *     <li>400, <code>{"errors": ["field name": ["error msg", ...], ...]}</code></li>
     *     <li>401</li>
     *     <li>403</li>
     * </ul>
     */
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
