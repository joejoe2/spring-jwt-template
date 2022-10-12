package com.joejoe2.demo.controller.constraint.checker;

import com.joejoe2.demo.controller.constraint.rate.LimitTarget;
import com.joejoe2.demo.controller.constraint.rate.RateLimit;
import com.joejoe2.demo.exception.ControllerConstraintViolation;
import com.joejoe2.demo.utils.AuthUtil;
import com.joejoe2.demo.utils.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ControllerRateConstraintChecker {
    private static final Logger logger = LoggerFactory.getLogger(ControllerAuthConstraintChecker.class);
    private static final int MAX_RETRY = 3;

    @Autowired
    private RedisTemplate<String, Map<String, Object>> redisTemplate;

    public void checkWithMethod(Method method) throws ControllerConstraintViolation {
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit==null) return;
        if (rateLimit.target() == LimitTarget.USER && !AuthUtil.isAuthenticated()) return;

        String targetIdentifier;
        switch (rateLimit.target()){
            case USER:
                targetIdentifier = AuthUtil.currentUserDetail().getId();
                break;
            case IP:
            default:
                targetIdentifier = IPUtils.getRequestIP();
        }
        checkRateLimitByTokenBucket(rateLimit.key(), targetIdentifier, rateLimit.limit(), rateLimit.period());
    }

    private void checkRateLimitByTokenBucket(String key, String targetIdentifier, long limit, long window) throws ControllerConstraintViolation {
        final boolean[] isExceed = {false};

        for (int retry = MAX_RETRY; retry>0; retry--){
            try {
                redisTemplate.execute(new SessionCallback<>() {
                    @Override
                    public List execute(RedisOperations operations) throws DataAccessException {
                        operations.watch(key+"_bucket_for_"+targetIdentifier);
                        Map bucket = operations.opsForHash().entries(key+"_bucket_for_"+targetIdentifier);
                        operations.multi();

                        long currentTime = System.currentTimeMillis();

                        if (bucket==null||bucket.isEmpty()){
                            bucket = new HashMap<>();
                            bucket.put("token", limit-1);
                            bucket.put("access_time", currentTime);
                        }else {
                            long tokens = ((Number)bucket.get("token")).longValue();
                            long refill = (long) (((currentTime - (long)bucket.get("access_time"))/1000)/(window*1.0/limit));
                            tokens = Math.min(tokens+refill, limit);
                            if (tokens>0){
                                bucket.put("token", tokens-1);
                                bucket.put("access_time", currentTime);
                            }else {
                                isExceed[0] = true;
                            }
                        }
                        operations.opsForHash().putAll(key+"_bucket_for_"+targetIdentifier, bucket);
                        operations.expire(key+"_bucket_for_"+targetIdentifier, Duration.ofSeconds(window));
                        return operations.exec();
                    }
                });
            }catch (Exception e){
                logger.error(e.getMessage());
                // retry to get/set rate limit because we use optimistic lock
                continue;
            }
            // exceed rate limit or not
            if (isExceed[0]){
                throw new ControllerConstraintViolation(429, "You have sent too many request, please try again later !");
            }else return;
        }
        // still cannot to get/set rate limit after retry
        throw new RuntimeException("cannot obtain rate limit from redis after retry for "+MAX_RETRY+" times !");
    }
}
