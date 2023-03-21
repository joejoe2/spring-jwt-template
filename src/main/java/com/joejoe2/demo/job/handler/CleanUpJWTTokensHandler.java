package com.joejoe2.demo.job.handler;

import com.joejoe2.demo.job.request.CleanUpJWTTokensJob;
import com.joejoe2.demo.service.jwt.JwtService;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CleanUpJWTTokensHandler implements JobRequestHandler<CleanUpJWTTokensJob> {
  @Autowired private JwtService jwtService;

  @Job(name = "delete all expired refresh tokens and related access tokens")
  @Override
  public void run(CleanUpJWTTokensJob job) throws Exception {
    jwtService.deleteExpiredTokens();
  }
}
