package com.joejoe2.demo.controller.constraint.checker;

import com.joejoe2.demo.controller.constraint.auth.ApiAllowsTo;
import com.joejoe2.demo.controller.constraint.auth.ApiRejectTo;
import com.joejoe2.demo.controller.constraint.auth.AuthenticatedApi;
import com.joejoe2.demo.exception.ControllerConstraintViolation;
import com.joejoe2.demo.utils.AuthUtil;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class ControllerAuthConstraintChecker {
    private static final int REJECT = 403, NOT_AUTHENTICATED = 401;

    public void checkWithMethod(Method method) throws ControllerConstraintViolation {
        checkAuthenticatedApiConstraint(method);

        for (Annotation constraint:method.getAnnotations()){
            if (constraint instanceof ApiAllowsTo apiAllowsTo){
                if (Arrays.stream(apiAllowsTo.roles())
                        .noneMatch((role)->role==AuthUtil.currentUserDetail().getRole()))
                    throw new ControllerConstraintViolation(
                            REJECT, apiAllowsTo.rejectMessage());
                else break;
            }else if(constraint instanceof ApiRejectTo apiRejectTo){
                if (Arrays.stream(apiRejectTo.roles())
                        .anyMatch((role -> role==AuthUtil.currentUserDetail().getRole())))
                    throw new ControllerConstraintViolation(
                            REJECT, apiRejectTo.rejectMessage());
                else break;
            }
        }
    }

    private static void checkAuthenticatedApiConstraint(Method method) throws ControllerConstraintViolation {
        AuthenticatedApi constraint = method.getAnnotation(AuthenticatedApi.class);
        if (constraint!=null){
            if (!AuthUtil.isAuthenticated())
                throw new ControllerConstraintViolation(
                        NOT_AUTHENTICATED, constraint.rejectMessage());
        }

        for (Annotation annotation:method.getAnnotations()){
            constraint = annotation.annotationType().getAnnotation(AuthenticatedApi.class);
            if (constraint!=null){
                if (!AuthUtil.isAuthenticated())
                    throw new ControllerConstraintViolation(NOT_AUTHENTICATED,
                            constraint.rejectMessage());
                else break;
            }
        }
    }
}
