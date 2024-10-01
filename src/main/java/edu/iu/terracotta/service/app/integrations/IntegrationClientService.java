package edu.iu.terracotta.service.app.integrations;

import java.util.List;

import edu.iu.terracotta.exceptions.integrations.IntegrationClientNotFoundException;
import edu.iu.terracotta.model.app.integrations.IntegrationClient;
import edu.iu.terracotta.model.app.integrations.dto.IntegrationClientDto;

public interface IntegrationClientService {

    List<IntegrationClient> getAll();
    List<IntegrationClientDto> toDto(List<IntegrationClient> integrationClients, String localUrl, String scoreVariable);
    IntegrationClientDto toDto(IntegrationClient integrationClient, String localUrl, String scoreVariable);
    IntegrationClient fromDto(IntegrationClientDto integrationClientDto) throws IntegrationClientNotFoundException;

}
