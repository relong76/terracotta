package edu.iu.terracotta.connectors.brightspace.service.lti.advantage.impl;

import org.springframework.stereotype.Service;

import edu.iu.terracotta.connectors.generic.annotation.TerracottaConnector;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.ToolDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.service.lti.advantage.AdvantageNoticeService;

@Service
@TerracottaConnector(LmsConnector.BRIGHTSPACE)
public class BrightspaceAdvantageNoticeServiceImpl implements AdvantageNoticeService {

    @Override
    public void ensureNoticeHandlerRegistered(ToolDeployment toolDeployment) {
        // course copy notices are Canvas only for now
    }

}
