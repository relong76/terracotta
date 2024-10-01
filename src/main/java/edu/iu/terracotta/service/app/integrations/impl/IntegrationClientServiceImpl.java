package edu.iu.terracotta.service.app.integrations.impl;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.exceptions.integrations.IntegrationClientNotFoundException;
import edu.iu.terracotta.model.app.integrations.IntegrationClient;
import edu.iu.terracotta.model.app.integrations.dto.IntegrationClientDto;
import edu.iu.terracotta.repository.integrations.IntegrationClientRepository;
import edu.iu.terracotta.service.app.integrations.IntegrationClientService;
import io.jsonwebtoken.lang.Collections;

@Service
public class IntegrationClientServiceImpl implements IntegrationClientService {

    @Autowired private IntegrationClientRepository integrationClientRepository;

    @Override
    public List<IntegrationClient> getAll() {
        return integrationClientRepository.getAllByEnabled(true);
    }

    @Override
    public List<IntegrationClientDto> toDto(List<IntegrationClient> integrationClients, String localUrl, String scoreVariable) {
        if (CollectionUtils.isEmpty(integrationClients)) {
            return Collections.emptyList();
        }

        return integrationClients.stream()
            .map(integrationClient -> toDto(integrationClient, localUrl, scoreVariable))
            .toList();
    }

    @Override
    public IntegrationClientDto toDto(IntegrationClient integrationClient, String localUrl, String scoreVariable) {
        if (integrationClient == null) {
            return null;
        }

        return IntegrationClientDto.builder()
            .customTokenVariableAllowed(integrationClient.isCustomTokenVariableAllowed())
            .id(integrationClient.getUuid())
            .name(integrationClient.getName())
            .returnUrl(
                String.format(
                    "%s/integrations?launch_token=%s&score=%s",
                    localUrl,
                    integrationClient.getTokenVariable(),
                    scoreVariable != null ? scoreVariable : "{{SCORE_VARIABLE}}"
                )
            )
            .tokenVariable(integrationClient.getTokenVariable())
            .build();
    }

    @Override
    public IntegrationClient fromDto(IntegrationClientDto integrationClientDto) throws IntegrationClientNotFoundException {
        return integrationClientRepository.findByUuid(integrationClientDto.getId())
            .orElseThrow(() -> new IntegrationClientNotFoundException(String.format("No integration client found for UUID: [%s]", integrationClientDto.getId())));
    }

}
