package edu.iu.terracotta.service.app.integrations;

import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenInvalidException;
import edu.iu.terracotta.exceptions.integrations.IntegrationTokenNotFoundException;
import edu.iu.terracotta.model.app.Submission;
import edu.iu.terracotta.model.app.integrations.IntegrationToken;

public interface IntegrationTokenService {

    void create(Submission submission, boolean isPreview);
    IntegrationToken findByToken(String token) throws IntegrationTokenNotFoundException;
    IntegrationToken redeemToken(String token) throws DataServiceException, IntegrationTokenNotFoundException, IntegrationTokenInvalidException;

}
