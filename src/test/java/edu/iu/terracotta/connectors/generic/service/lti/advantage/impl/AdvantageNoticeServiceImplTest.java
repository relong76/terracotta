package edu.iu.terracotta.connectors.generic.service.lti.advantage.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.iu.terracotta.base.BaseTest;
import edu.iu.terracotta.connectors.brightspace.service.lti.advantage.impl.BrightspaceAdvantageNoticeServiceImpl;
import edu.iu.terracotta.connectors.generic.exceptions.TerracottaConnectorException;
import edu.iu.terracotta.connectors.generic.service.connector.ConnectorService;
import edu.iu.terracotta.connectors.generic.service.lti.advantage.AdvantageNoticeService;
import edu.iu.terracotta.connectors.oneedtech.service.lti.advantage.impl.OneEdTechAdvantageNoticeServiceImpl;

public class AdvantageNoticeServiceImplTest extends BaseTest {

    // wired by hand: ConnectorService's type parameter is erased, so @InjectMocks can't tell the mocks apart
    @Mock private ConnectorService<AdvantageNoticeService> advantageNoticeConnectorService;
    @Mock private AdvantageNoticeService resolvedAdvantageNoticeService;

    private AdvantageNoticeServiceImpl advantageNoticeService;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        setup();

        when(toolDeployment.getPlatformDeployment()).thenReturn(platformDeployment);
        advantageNoticeService = new AdvantageNoticeServiceImpl(advantageNoticeConnectorService);
    }

    @Test
    public void testDelegatesToTheDeploymentsConnector() throws TerracottaConnectorException {
        when(advantageNoticeConnectorService.instance(platformDeployment, AdvantageNoticeService.class)).thenReturn(resolvedAdvantageNoticeService);

        advantageNoticeService.ensureNoticeHandlerRegistered(toolDeployment);

        verify(resolvedAdvantageNoticeService).ensureNoticeHandlerRegistered(toolDeployment);
    }

    // the launch goes on without it
    @Test
    public void testAMissingConnectorDoesntFailTheLaunch() throws TerracottaConnectorException {
        when(advantageNoticeConnectorService.instance(platformDeployment, AdvantageNoticeService.class)).thenThrow(new TerracottaConnectorException("no connector"));

        assertDoesNotThrow(() -> advantageNoticeService.ensureNoticeHandlerRegistered(toolDeployment));
        verifyNoInteractions(resolvedAdvantageNoticeService);
    }

    // course copy notices are Canvas only for now
    @Test
    public void testOtherLmsConnectorsDoNothing() {
        new BrightspaceAdvantageNoticeServiceImpl().ensureNoticeHandlerRegistered(toolDeployment);
        new OneEdTechAdvantageNoticeServiceImpl().ensureNoticeHandlerRegistered(toolDeployment);

        verifyNoInteractions(toolDeployment);
    }

}
