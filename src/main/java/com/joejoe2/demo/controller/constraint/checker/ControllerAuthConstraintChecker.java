package com.joejoe2.demo.controller.constraint.checker;

import com.joejoe2.demo.controller.constraint.auth.ApiAllowsTo;
import com.joejoe2.demo.controller.constraint.auth.ApiRejectTo;
import com.joejoe2.demo.controller.constraint.auth.AuthenticatedApi;
import com.joejoe2.demo.exception.ControllerAuthConstraintViolation;
import com.joejoe2.demo.utils.AuthUtil;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class ControllerAuthConstraintChecker {
    public static void checkWithMethod(HandlerMethod method) throws ControllerAuthConstraintViolation {
        checkAuthenticatedApiConstraint(method);

        for (Annotation constraint:method.getMethod().getAnnotations()){
            if (constraint instanceof ApiAllowsTo apiAllowsTo){
                if (Arrays.stream(apiAllowsTo.roles())
                        .noneMatch((role)->role==AuthUtil.currentUserDetail().getRole()))
                    throw new ControllerAuthConstraintViolation(
                            apiAllowsTo.rejectStatus(), apiAllowsTo.rejectMessage());
            }else if(constraint instanceof ApiRejectTo apiRejectTo){
                if (Arrays.stream(apiRejectTo.roles())
                        .anyMatch((role -> role==AuthUtil.currentUserDetail().getRole())))
                    throw new ControllerAuthConstraintViolation(
                            apiRejectTo.rejectStatus(), apiRejectTo.rejectMessage());
            }
        }
    }

    private static void checkAuthenticatedApiConstraint(HandlerMethod method) throws ControllerAuthConstraintViolation{
        AuthenticatedApi constraint = method.getMethod().getAnnotation(AuthenticatedApi.class);
        if (constraint!=null){
            if (!AuthUtil.isAuthenticated())
                throw new ControllerAuthConstraintViolation(
                        constraint.rejectStatus(), constraint.rejectMessage());
        }

        for (Annotation annotation:method.getMethod().getAnnotations()){
            constraint = annotation.annotationType().getAnnotation(AuthenticatedApi.class);
            if (constraint!=null){
                if (!AuthUtil.isAuthenticated())
                    throw new ControllerAuthConstraintViolation(constraint.rejectStatus(),
                            constraint.rejectMessage());
                else break;
            }
        }
    }
}
