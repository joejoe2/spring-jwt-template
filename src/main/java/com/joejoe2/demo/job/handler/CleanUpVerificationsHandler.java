package com.joejoe2.demo.job.handler;

import com.joejoe2.demo.job.request.CleanUpVerificationsJob;
import com.joejoe2.demo.service.verification.VerificationService;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CleanUpVerificationsHandler implements JobRequestHandler<CleanUpVerificationsJob> {
  @Autowired VerificationService verificationService;

  @Job(name = "delete all expired verification codes and tokens")
  @Override
  public void run(CleanUpVerificationsJob job) throws Exception {
    verificationService.deleteExpiredVerificationCodes();
    verificationService.deleteExpiredVerifyTokens();
  }
}
