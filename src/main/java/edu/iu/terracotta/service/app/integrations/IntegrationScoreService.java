package edu.iu.terracotta.service.app.integrations;

import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenInvalidException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenNotFoundException;

public interface IntegrationScoreService {

    void score(String launchToken, String score) throws IntegrationTokenNotFoundException, DataServiceException, IntegrationTokenInvalidException;

}
