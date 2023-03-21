package com.joejoe2.demo.init;

import com.joejoe2.demo.job.request.CleanUpJWTTokensJob;
import com.joejoe2.demo.job.request.CleanUpVerificationsJob;
import java.time.Duration;
import org.jobrunr.scheduling.BackgroundJobRequest;
import org.jobrunr.storage.StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JobRunrInitializer implements CommandLineRunner {
  @Autowired private Environment env;
  @Autowired StorageProvider storageProvider;

  @Override
  public void run(String... args) throws Exception {
    createRecurrentJob(env);
  }

  private void createRecurrentJob(Environment env) {
    if (!env.getProperty("init.recurrent-job", Boolean.class, true)) {
      storageProvider
          .getRecurringJobs()
          .forEach((recurringJob -> BackgroundJobRequest.delete(recurringJob.getId())));
      return;
    }
    BackgroundJobRequest.scheduleRecurrently(
        "CleanUpJWTTokens", Duration.ofSeconds(1800), new CleanUpJWTTokensJob());
    BackgroundJobRequest.scheduleRecurrently(
        "CleanUpVerifications", Duration.ofSeconds(1800), new CleanUpVerificationsJob());
  }
}
