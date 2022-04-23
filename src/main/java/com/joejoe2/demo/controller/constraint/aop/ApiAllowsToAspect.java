package com.joejoe2.demo.controller.constraint.aop;

import com.joejoe2.demo.controller.constraint.ApiAllowsTo;
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

import java.util.Arrays;
import java.util.Collections;

@Aspect
@Component
@Order(50)
public class ApiAllowsToAspect {
    //match with annotation
    @Pointcut("@annotation(com.joejoe2.demo.controller.constraint.ApiAllowsTo)")
    public void pointCut() {
    }


    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        ApiAllowsTo apiAllowsTo = getAnnotation(point);
        if (AuthUtil.isAuthenticated() && Arrays.stream(apiAllowsTo.roles())
                .anyMatch((role)->role==AuthUtil.currentUserDetail().getRole())) {
            return point.proceed();
        }else{
            if (apiAllowsTo.rejectMessage().isEmpty())
                return new ResponseEntity<>(HttpStatus.valueOf(apiAllowsTo.rejectStatus()));
            else return new ResponseEntity<>(Collections
                    .singletonMap("message", apiAllowsTo.rejectMessage()),
                    HttpStatus.valueOf(apiAllowsTo.rejectStatus()));
        }
    }

    private ApiAllowsTo getAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getAnnotation(ApiAllowsTo.class);
    }
}
