package edu.iu.terracotta.service.app.integrations;

import edu.iu.terracotta.model.app.Submission;

public interface IntegrationLaunchParameterService {

    String buildQueryString(Submission submission, int submissionCount);

}
