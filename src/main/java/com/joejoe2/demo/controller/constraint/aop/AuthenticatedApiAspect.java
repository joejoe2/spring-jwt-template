package com.joejoe2.demo.controller.constraint.aop;

import com.joejoe2.demo.controller.constraint.AuthenticatedApi;
import com.joejoe2.demo.utils.AuthUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Collections;

@Aspect
@Order(49)
@Component
public class AuthenticatedApiAspect {
    //match with inner annotation
    @Pointcut("execution(@(@com.joejoe2.demo.controller.constraint.AuthenticatedApi *) * *(..))")
    public void innerPointCut() {
    }

    //match with annotation
    @Pointcut("@annotation(com.joejoe2.demo.controller.constraint.AuthenticatedApi)")
    public void pointCut() {
    }


    @Around("innerPointCut() || pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        AuthenticatedApi apiAllowsTo = getAnnotation(point);
        if (AuthUtil.isAuthenticated()) {
            if (AuthUtil.currentUserDetail().isEnabled())
                return point.proceed();
            else
                return new ResponseEntity<>(Collections
                        .singletonMap("message", "User is disabled !"),
                        HttpStatus.FORBIDDEN);
        }else {
            if (apiAllowsTo.rejectMessage().isEmpty())
                return new ResponseEntity<>(HttpStatus.valueOf(apiAllowsTo.rejectStatus()));
            else return new ResponseEntity<>(Collections
                    .singletonMap("message", apiAllowsTo.rejectMessage()),
                    HttpStatus.valueOf(apiAllowsTo.rejectStatus()));
        }
    }

    private AuthenticatedApi getAnnotation(JoinPoint joinPoint) throws Exception{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        AuthenticatedApi res = signature.getMethod().getAnnotation(AuthenticatedApi.class);
        if (res!=null)return res;

        for (Annotation annotation:signature.getMethod().getAnnotations()){
            res = annotation.annotationType().getAnnotation(AuthenticatedApi.class);
            if (res!=null)return res;
        }
        throw new Exception("cannot find "+AuthenticatedApi.class.getCanonicalName()+" on first level");
    }
}
