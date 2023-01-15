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

    public void checkWithMethod(Method method) throws ControllerConstraintViolation {
        checkAuthenticatedApiConstraint(method);
        checkRoleConstraints(method);
    }

    private static void checkAuthenticatedApiConstraint(Method method) throws ControllerConstraintViolation {
        //why AnnotatedElementUtils not work at here ?
        AuthenticatedApi constraint = method.getAnnotation(AuthenticatedApi.class);
        //direct check
        if (constraint != null) {
            if (!AuthUtil.isAuthenticated())
                throw new ControllerConstraintViolation(
                        constraint.rejectStatus(), constraint.rejectMessage());
        }
        //check for one level in composed annotations
        for (Annotation annotation : method.getAnnotations()) {
            constraint = annotation.annotationType().getAnnotation(AuthenticatedApi.class);
            if (constraint != null) {
                if (!AuthUtil.isAuthenticated())
                    throw new ControllerConstraintViolation(constraint.rejectStatus(), constraint.rejectMessage());
                else break;
            }
        }
    }

    private static void checkRoleConstraints(Method method) throws ControllerConstraintViolation {
        for (Annotation constraint : method.getAnnotations()) {
            if (constraint instanceof ApiAllowsTo apiAllowsTo) {
                if (Arrays.stream(apiAllowsTo.roles())
                        .noneMatch((role) -> role == AuthUtil.currentUserDetail().getRole()))
                    throw new ControllerConstraintViolation(
                            apiAllowsTo.rejectStatus(), apiAllowsTo.rejectMessage());
                else break;
            } else if (constraint instanceof ApiRejectTo apiRejectTo) {
                if (Arrays.stream(apiRejectTo.roles())
                        .anyMatch((role -> role == AuthUtil.currentUserDetail().getRole())))
                    throw new ControllerConstraintViolation(
                            apiRejectTo.rejectStatus(), apiRejectTo.rejectMessage());
                else break;
            }
        }
    }
}
