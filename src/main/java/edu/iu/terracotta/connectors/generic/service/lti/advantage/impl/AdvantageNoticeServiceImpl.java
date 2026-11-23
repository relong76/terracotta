package edu.iu.terracotta.connectors.generic.service.lti.advantage.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.ToolDeployment;
import edu.iu.terracotta.connectors.generic.exceptions.TerracottaConnectorException;
import edu.iu.terracotta.connectors.generic.service.connector.ConnectorService;
import edu.iu.terracotta.connectors.generic.service.lti.advantage.AdvantageNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.GuardLogStatement"})
public class AdvantageNoticeServiceImpl implements AdvantageNoticeService {

    private final ConnectorService<AdvantageNoticeService> connectorService;

    @Override
    public void ensureNoticeHandlerRegistered(ToolDeployment toolDeployment) {
        try {
            connectorService.instance(toolDeployment.getPlatformDeployment(), AdvantageNoticeService.class).ensureNoticeHandlerRegistered(toolDeployment);
        } catch (TerracottaConnectorException e) {
            // the launch goes on without it
            log.warn("Couldn't register the notice handler for tool deployment ID: [{}]: {}", toolDeployment.getDeploymentId(), e.getMessage());
        }
    }

}
