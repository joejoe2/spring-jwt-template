package com.joejoe2.demo.init;

import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.service.verification.VerificationService;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JobRunrInitializer implements CommandLineRunner {
    @Autowired
    private Environment env;
    @Autowired
    private JobScheduler jobScheduler;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private VerificationService verificationService;

    @Override
    public void run(String... args) throws Exception {
        createRecurrentJob(env);
    }

    private void createRecurrentJob(Environment env) {
        if (!env.getProperty("init.recurrent-job", Boolean.class, true)) return;
        jobScheduler.scheduleRecurrently(Duration.ofMinutes(30), () -> jwtService.deleteExpiredTokens());
        jobScheduler.scheduleRecurrently(Duration.ofMinutes(30), () -> verificationService.deleteExpiredVerificationCodes());
        jobScheduler.scheduleRecurrently(Duration.ofMinutes(30), () -> verificationService.deleteExpiredVerifyTokens());
    }
}
