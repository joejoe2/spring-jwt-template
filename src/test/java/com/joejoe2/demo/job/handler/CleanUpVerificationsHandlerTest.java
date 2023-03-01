package com.joejoe2.demo.job.handler;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.job.request.CleanUpVerificationsJob;
import com.joejoe2.demo.service.verification.VerificationService;
import org.jobrunr.scheduling.BackgroundJobRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class CleanUpVerificationsHandlerTest {
    UUID id = UUID.randomUUID();
    @SpyBean
    VerificationService verificationService;
    @SpyBean
    CleanUpVerificationsHandler handler;

    @AfterEach
    void tearDown() {
        BackgroundJobRequest.delete(id);
    }

    @Test
    void run() throws Exception {
        BackgroundJobRequest.enqueue(id, new CleanUpVerificationsJob());
        Thread.sleep(20000);
        Mockito.verify(handler, Mockito.atLeastOnce()).run(Mockito.any());
        Mockito.verify(verificationService, Mockito.atLeastOnce()).deleteExpiredVerificationCodes();
        Mockito.verify(verificationService, Mockito.atLeastOnce()).deleteExpiredVerifyTokens();
    }
}