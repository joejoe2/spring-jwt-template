package com.joejoe2.demo.interceptor;

import com.joejoe2.demo.controller.constraint.checker.ControllerAuthConstraintChecker;
import com.joejoe2.demo.exception.ControllerAuthConstraintViolation;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ControllerConstraintInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            checkControllerAuthConstraint((HandlerMethod) handler);
        }catch (ControllerAuthConstraintViolation ex){
            setJsonResponse(response, ex.getRejectStatus(), ex.getRejectMessage());
            return false;
        }
        return true;
    }

    private void checkControllerAuthConstraint(HandlerMethod method) throws ControllerAuthConstraintViolation {
        ControllerAuthConstraintChecker.checkWithMethod(method);
    }

    private void setJsonResponse(HttpServletResponse response, int status, String message){
        if (message!=null&&!message.isEmpty()){
            try {
                response.getWriter().write("{ \"message\": \""+message+"\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
    }
}
