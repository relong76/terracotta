package edu.iu.terracotta.service.app.integrations;

import java.util.UUID;

import edu.iu.terracotta.exceptions.integrations.IntegrationClientNotFoundException;
import edu.iu.terracotta.exceptions.integrations.IntegrationConfigurationNotFoundException;
import edu.iu.terracotta.exceptions.integrations.IntegrationConfigurationNotMatchingException;
import edu.iu.terracotta.model.app.integrations.Integration;
import edu.iu.terracotta.model.app.integrations.IntegrationConfiguration;
import edu.iu.terracotta.model.app.integrations.dto.IntegrationConfigurationDto;

public interface IntegrationConfigurationService {

    IntegrationConfiguration create(Integration integration);
    IntegrationConfiguration update(IntegrationConfigurationDto integrationConfigurationDto, Integration integration) throws IntegrationConfigurationNotFoundException, IntegrationConfigurationNotMatchingException, IntegrationClientNotFoundException;
    void delete(IntegrationConfiguration integrationConfiguration);
    void duplicate(IntegrationConfiguration integrationConfiguration, Integration integration);
    IntegrationConfigurationDto toDto(IntegrationConfiguration integrationConfiguration, UUID integrationUuid);
    IntegrationConfiguration fromDto(IntegrationConfigurationDto integrationConfigurationDto, IntegrationConfiguration integrationConfiguration) throws IntegrationClientNotFoundException;

}
