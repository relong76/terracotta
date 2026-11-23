package edu.iu.terracotta.connectors.generic.service.lti.advantage;

import edu.iu.terracotta.connectors.generic.annotation.TerracottaConnector;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.ToolDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;

/**
 * Registers this tool as a handler for the LMS's LTI Advantage Platform Notification Service
 * course copy notice (LtiContextCopyNotice), so NoticeController actually gets called. Each LMS
 * connector decides whether it supports this; one that doesn't does nothing.
 */
@TerracottaConnector(LmsConnector.GENERIC)
public interface AdvantageNoticeService {

    /**
     * Safe to call on every launch: never throws, and never blocks or fails the launch. Each
     * connector skips the work once it's done for this deployment.
     */
    void ensureNoticeHandlerRegistered(ToolDeployment toolDeployment);

}
