package com.joejoe2.demo.controller.constraint.aop;

import com.joejoe2.demo.controller.constraint.ApiRejectTo;
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
@Order(50)
@Component
public class ApiRejectToAspect {
    //match with annotation
    @Pointcut("@annotation(com.joejoe2.demo.controller.constraint.ApiRejectTo)")
    public void pointCut() {
    }


    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        ApiRejectTo apiRejectTo = getAnnotation(point);
        if (AuthUtil.isAuthenticated() && Arrays.stream(apiRejectTo.roles())
                .anyMatch((role)->role==AuthUtil.currentUserDetail().getRole())) {
            if (apiRejectTo.rejectMessage().isEmpty())
                return new ResponseEntity<>(HttpStatus.valueOf(apiRejectTo.rejectStatus()));
            else return new ResponseEntity<>(Collections
                    .singletonMap("message", apiRejectTo.rejectMessage()),
                    HttpStatus.valueOf(apiRejectTo.rejectStatus()));
        }else{
            return point.proceed();
        }
    }

    private ApiRejectTo getAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getAnnotation(ApiRejectTo.class);
    }
}
