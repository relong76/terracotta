package edu.iu.terracotta.service.app.integrations.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.integrations.IntegrationClientNotFoundException;
import edu.iu.terracotta.exceptions.integrations.IntegrationConfigurationNotFoundException;
import edu.iu.terracotta.exceptions.integrations.IntegrationConfigurationNotMatchingException;
import edu.iu.terracotta.model.app.integrations.Integration;
import edu.iu.terracotta.model.app.integrations.IntegrationConfiguration;
import edu.iu.terracotta.model.app.integrations.dto.IntegrationConfigurationDto;
import edu.iu.terracotta.repository.integrations.IntegrationConfigurationRepository;
import edu.iu.terracotta.repository.integrations.IntegrationRepository;
import edu.iu.terracotta.service.app.integrations.IntegrationClientService;
import edu.iu.terracotta.service.app.integrations.IntegrationConfigurationService;

@Service
public class IntegrationConfigurationServiceImpl implements IntegrationConfigurationService {

    @Autowired private IntegrationClientService integrationClientService;
    @Autowired private IntegrationConfigurationRepository integrationConfigurationRepository;
    @Autowired private IntegrationRepository integrationRepository;

    @Override
    public IntegrationConfiguration create(Integration integration) {
        IntegrationConfiguration integrationConfiguration = integrationConfigurationRepository.save(
            IntegrationConfiguration.builder()
                .integration(integration)
                .build()
        );

        integration.setConfiguration(integrationConfiguration);
        integrationRepository.save(integration);

        return integrationConfiguration;
    }

    @Override
    public IntegrationConfiguration update(IntegrationConfigurationDto integrationConfigurationDto, Integration integration) throws IntegrationConfigurationNotFoundException, IntegrationConfigurationNotMatchingException, IntegrationClientNotFoundException {
        if (!integration.getConfiguration().getUuid().equals(integrationConfigurationDto.getId())) {
            throw new IntegrationConfigurationNotMatchingException(String.format("Integration configuration with UUID: [%s] does not match the given integration with ID: [%s].", integrationConfigurationDto.getId(), integration.getId()));
        }

        IntegrationConfiguration integrationConfiguration = integrationConfigurationRepository.findById(integration.getConfiguration().getId())
            .orElseThrow(() -> new IntegrationConfigurationNotFoundException(String.format("No integration configuration with ID: [%s] found.", integration.getConfiguration().getId())));

        return integrationConfigurationRepository.save(
            fromDto(integrationConfigurationDto, integrationConfiguration)
        );
    }

    @Override
    public void delete(IntegrationConfiguration integrationConfiguration) {
        if (integrationConfiguration == null) {
            return;
        }

        integrationConfigurationRepository.deleteById(integrationConfiguration.getId());
    }

    @Override
    public void duplicate(IntegrationConfiguration integrationConfiguration, Integration integration) {
        IntegrationConfiguration newIntegrationConfiguration = integrationConfigurationRepository.save(
            IntegrationConfiguration.builder()
                .client(integrationConfiguration.getClient())
                .integration(integration)
                .launchUrl(integrationConfiguration.getLaunchUrl())
                .scoreVariable(integrationConfiguration.getScoreVariable())
                .tokenVariable(integrationConfiguration.getTokenVariable())
                .build()
        );

        integration.setConfiguration(newIntegrationConfiguration);
    }

    @Override
    public IntegrationConfigurationDto toDto(IntegrationConfiguration integrationConfiguration, UUID integrationUuid) {
        if (integrationConfiguration == null) {
            return null;
        }

        return IntegrationConfigurationDto.builder()
            .client(
                integrationClientService.toDto(
                    integrationConfiguration.getClient(),
                    integrationConfiguration.getIntegration().getLocalUrl(),
                    integrationConfiguration.getScoreVariable()))
            .id(integrationConfiguration.getUuid())
            .launchUrl(integrationConfiguration.getLaunchUrl())
            .scoreVariable(integrationConfiguration.getScoreVariable())
            .tokenVariable(integrationConfiguration.getTokenVariable())
            .build();
    }

    @Override
    public IntegrationConfiguration fromDto(IntegrationConfigurationDto integrationConfigurationDto, IntegrationConfiguration integrationConfiguration) throws IntegrationClientNotFoundException {
        if (integrationConfiguration == null) {
            integrationConfiguration = IntegrationConfiguration.builder().build();
        }

        if (integrationConfigurationDto == null) {
            return integrationConfiguration;
        }

        integrationConfiguration.setClient(integrationClientService.fromDto(integrationConfigurationDto.getClient()));
        integrationConfiguration.setLaunchUrl(integrationConfigurationDto.getLaunchUrl());
        integrationConfiguration.setScoreVariable(integrationConfigurationDto.getScoreVariable());
        integrationConfiguration.setTokenVariable(
            integrationConfigurationDto.getTokenVariable() != null ? integrationConfigurationDto.getTokenVariable() : integrationConfiguration.getClient().getTokenVariable()
        );

        return integrationConfiguration;
    }

}
