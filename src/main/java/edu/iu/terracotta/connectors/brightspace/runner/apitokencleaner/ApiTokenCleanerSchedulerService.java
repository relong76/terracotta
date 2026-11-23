package edu.iu.terracotta.connectors.brightspace.runner.apitokencleaner;

import java.util.Optional;

import edu.iu.terracotta.connectors.brightspace.runner.apitokencleaner.model.ApiTokenCleanerScheduleResult;

public interface ApiTokenCleanerSchedulerService {

    Optional<ApiTokenCleanerScheduleResult> cleanup(int expirationTtlDays);

}
