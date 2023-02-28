package com.joejoe2.demo.job.request;

import com.joejoe2.demo.job.handler.CleanUpVerificationsHandler;
import lombok.Data;
import org.jobrunr.jobs.lambdas.JobRequest;

@Data
public class CleanUpVerificationsJob implements JobRequest {
    @Override
    public Class<CleanUpVerificationsHandler> getJobRequestHandler() {
        return CleanUpVerificationsHandler.class;
    }
}
