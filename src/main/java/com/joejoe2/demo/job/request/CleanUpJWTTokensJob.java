package com.joejoe2.demo.job.request;

import com.joejoe2.demo.job.handler.CleanUpJWTTokensHandler;
import lombok.Data;
import org.jobrunr.jobs.lambdas.JobRequest;

@Data
public class CleanUpJWTTokensJob implements JobRequest {
    @Override
    public Class<CleanUpJWTTokensHandler> getJobRequestHandler() {
        return CleanUpJWTTokensHandler.class;
    }
}
