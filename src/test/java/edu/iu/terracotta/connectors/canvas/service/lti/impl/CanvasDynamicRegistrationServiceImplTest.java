package edu.iu.terracotta.connectors.canvas.service.lti.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import edu.iu.terracotta.connectors.canvas.service.lti.CanvasRegistrationException;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.ToolDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.PlatformDeploymentRepository;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.ToolDeploymentRepository;
import edu.iu.terracotta.connectors.generic.service.lti.LmsApprovedAccountService;
import edu.iu.terracotta.service.app.OutboundUrlService;

public class CanvasDynamicRegistrationServiceImplTest {

    private static final String OPENID_CONFIGURATION = "https://iu.instructure.com/api/lti/security/openid-configuration";
    private static final String REGISTRATION_ENDPOINT = "https://iu.instructure.com/api/lti/registrations";
    private static final String TOKEN = "one-time-registration-token";

    private static final String PLATFORM_CONFIGURATION = """
        {
          "issuer": "https://canvas.instructure.com",
          "authorization_endpoint": "https://sso.canvaslms.com/api/lti/authorize_redirect",
          "token_endpoint": "https://sso.canvaslms.com/login/oauth2/token",
          "jwks_uri": "https://sso.canvaslms.com/api/lti/security/jwks",
          "registration_endpoint": "%s",
          "https://purl.imsglobal.org/spec/lti-platform-configuration": { "product_family_code": "%s" }
        }
        """;

    private static final String REGISTRATION_RESPONSE = """
        {
          "client_id": "10000000000042",
          "https://purl.imsglobal.org/spec/lti-tool-configuration": { "deployment_id": "42:abc123" }
        }
        """;

    private OutboundUrlService outboundUrlService;
    private PlatformDeploymentRepository platformDeploymentRepository;
    private ToolDeploymentRepository toolDeploymentRepository;
    private LmsApprovedAccountService lmsApprovedAccountService;
    private LmsApprovedAccount approvedAccount;
    private CanvasDynamicRegistrationServiceImpl service;
    private MockRestServiceServer canvas;

    @BeforeEach
    public void beforeEach() {
        outboundUrlService = mock(OutboundUrlService.class);
        platformDeploymentRepository = mock(PlatformDeploymentRepository.class);
        toolDeploymentRepository = mock(ToolDeploymentRepository.class);
        lmsApprovedAccountService = mock(LmsApprovedAccountService.class);
        approvedAccount = LmsApprovedAccount.builder().name("Indiana University").lmsConnector(LmsConnector.CANVAS).approved(true).build();

        CanvasToolConfigurationServiceImpl canvasToolConfigurationService = new CanvasToolConfigurationServiceImpl();
        ReflectionTestUtils.setField(canvasToolConfigurationService, "applicationUrl", "https://app.terracotta.education");
        ReflectionTestUtils.setField(canvasToolConfigurationService, "title", "Terracotta");
        ReflectionTestUtils.setField(canvasToolConfigurationService, "description", "Terracotta");

        service = new CanvasDynamicRegistrationServiceImpl(
            canvasToolConfigurationService,
            outboundUrlService,
            platformDeploymentRepository,
            toolDeploymentRepository,
            lmsApprovedAccountService,
            "https://app.terracotta.education/"
        );

        RestTemplate restTemplate = new RestTemplate();
        canvas = MockRestServiceServer.bindTo(restTemplate).build();
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

        when(lmsApprovedAccountService.findOrCreate(LmsConnector.CANVAS, "iu.instructure.com")).thenReturn(approvedAccount);
        when(platformDeploymentRepository.findByIssAndClientId(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(platformDeploymentRepository.save(any(PlatformDeployment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(toolDeploymentRepository.findByPlatformDeployment_IssAndPlatformDeployment_ClientIdAndLtiDeploymentId(anyString(), anyString(), anyString())).thenReturn(Collections.emptyList());
    }

    private void canvasServes(String registrationEndpoint, String productFamily) {
        canvas.expect(requestTo(OPENID_CONFIGURATION))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andRespond(withSuccess(String.format(PLATFORM_CONFIGURATION, registrationEndpoint, productFamily), MediaType.APPLICATION_JSON));
    }

    @Test
    public void testRegistersAnApprovedCanvasAndSavesItsDeployment() throws Exception {
        canvasServes(REGISTRATION_ENDPOINT, "canvas");
        canvas.expect(requestTo(REGISTRATION_ENDPOINT))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.client_name").value("Terracotta"))
            .andExpect(jsonPath("$.initiate_login_uri").value("https://app.terracotta.education/oidc/login_initiations"))
            .andExpect(jsonPath("$.scope").isString())
            .andExpect(jsonPath("$['https://purl.imsglobal.org/spec/lti-tool-configuration'].messages[0].placements[0]").value("course_navigation"))
            .andRespond(withSuccess(REGISTRATION_RESPONSE, MediaType.APPLICATION_JSON));

        PlatformDeployment saved = service.register(OPENID_CONFIGURATION, TOKEN);

        canvas.verify();
        assertEquals("https://canvas.instructure.com", saved.getIss());
        assertEquals("10000000000042", saved.getClientId());
        assertEquals("https://sso.canvaslms.com/api/lti/authorize_redirect", saved.getOidcEndpoint());
        assertEquals("https://sso.canvaslms.com/api/lti/security/jwks", saved.getJwksEndpoint());
        assertEquals("https://sso.canvaslms.com/login/oauth2/token", saved.getOAuth2TokenUrl());
        assertEquals("https://iu.instructure.com", saved.getBaseUrl());
        assertEquals("https://app.terracotta.education", saved.getLocalUrl());
        assertEquals(LmsConnector.CANVAS, saved.getLmsConnector());
        assertTrue(saved.getEnableAutomaticDeployments());
        assertSame(approvedAccount, saved.getLmsApprovedAccount());

        ArgumentCaptor<ToolDeployment> toolDeployment = ArgumentCaptor.forClass(ToolDeployment.class);
        verify(toolDeploymentRepository).save(toolDeployment.capture());
        assertEquals("42:abc123", toolDeployment.getValue().getLtiDeploymentId());
        assertEquals(saved, toolDeployment.getValue().getPlatformDeployment());
    }

    @Test
    public void testACanvasWithNoApprovedAccountRegistersButWaitsForApproval() throws Exception {
        String other = "https://other.instructure.com/api/lti/security/openid-configuration";
        LmsApprovedAccount pending = LmsApprovedAccount.builder().name("other.instructure.com").lmsConnector(LmsConnector.CANVAS).approved(false).build();
        when(lmsApprovedAccountService.findOrCreate(LmsConnector.CANVAS, "other.instructure.com")).thenReturn(pending);
        canvas.expect(requestTo(other))
            .andRespond(withSuccess(String.format(PLATFORM_CONFIGURATION, "https://other.instructure.com/api/lti/registrations", "canvas"), MediaType.APPLICATION_JSON));
        canvas.expect(requestTo("https://other.instructure.com/api/lti/registrations"))
            .andRespond(withSuccess(REGISTRATION_RESPONSE, MediaType.APPLICATION_JSON));

        PlatformDeployment saved = service.register(other, TOKEN);

        canvas.verify();
        assertSame(pending, saved.getLmsApprovedAccount());
        assertFalse(saved.getLmsApprovedAccount().isApproved());
        assertEquals("https://other.instructure.com", saved.getBaseUrl());
    }

    @Test
    public void testNoAccountIsCreatedWhenCanvasRejectsTheRegistration() {
        canvasServes(REGISTRATION_ENDPOINT, "canvas");
        canvas.expect(requestTo(REGISTRATION_ENDPOINT)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, TOKEN));
        verify(lmsApprovedAccountService, never()).findOrCreate(any(), any());
    }

    @Test
    public void testRefusesAnAddressThatIsntHttps() {
        assertThrows(CanvasRegistrationException.class, () -> service.register("http://iu.instructure.com/api/lti/security/openid-configuration", TOKEN));
        assertThrows(CanvasRegistrationException.class, () -> service.register("not a url", TOKEN));
        verify(platformDeploymentRepository, never()).save(any());
    }

    @Test
    public void testNeedsBothValuesFromCanvas() {
        assertThrows(CanvasRegistrationException.class, () -> service.register(null, TOKEN));
        assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, " "));
    }

    @Test
    public void testRefusesARegistrationEndpointOnAnotherHost() {
        canvasServes("https://attacker.example.com/register", "canvas");

        assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, TOKEN));

        canvas.verify();
        verify(platformDeploymentRepository, never()).save(any());
    }

    @Test
    public void testRefusesAPlatformThatIsntCanvas() {
        canvasServes(REGISTRATION_ENDPOINT, "moodle");

        assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, TOKEN));
        verify(platformDeploymentRepository, never()).save(any());
    }

    @Test
    public void testRefusesAConfigurationMissingAnEndpoint() {
        canvas.expect(requestTo(OPENID_CONFIGURATION))
            .andRespond(withSuccess("{\"issuer\": \"https://canvas.instructure.com\"}", MediaType.APPLICATION_JSON));

        assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, TOKEN));
    }

    @Test
    public void testReportsACanvasThatRejectsTheRegistration() {
        canvasServes(REGISTRATION_ENDPOINT, "canvas");
        canvas.expect(requestTo(REGISTRATION_ENDPOINT)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        CanvasRegistrationException exception = assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, TOKEN));

        assertTrue(exception.getMessage().contains("didn't accept"), exception.getMessage());
        verify(platformDeploymentRepository, never()).save(any());
    }

    @Test
    public void testNeedsAClientIdInCanvassResponse() {
        canvasServes(REGISTRATION_ENDPOINT, "canvas");
        canvas.expect(requestTo(REGISTRATION_ENDPOINT)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, TOKEN));
        verify(platformDeploymentRepository, never()).save(any());
    }

    @Test
    public void testRefusesAnInternalAddress() {
        doThrow(new IllegalArgumentException("internal")).when(outboundUrlService).validatePublicUrl(OPENID_CONFIGURATION);

        assertThrows(CanvasRegistrationException.class, () -> service.register(OPENID_CONFIGURATION, TOKEN));
    }

    @Test
    public void testRegisteringAgainUpdatesTheExistingRecordAndDeployment() throws Exception {
        PlatformDeployment existing = new PlatformDeployment();
        existing.setKeyId(7L);
        when(platformDeploymentRepository.findByIssAndClientId("https://canvas.instructure.com", "10000000000042")).thenReturn(List.of(existing));
        when(toolDeploymentRepository.findByPlatformDeployment_IssAndPlatformDeployment_ClientIdAndLtiDeploymentId("https://canvas.instructure.com", "10000000000042", "42:abc123"))
            .thenReturn(List.of(new ToolDeployment()));
        canvasServes(REGISTRATION_ENDPOINT, "canvas");
        canvas.expect(requestTo(REGISTRATION_ENDPOINT)).andRespond(withSuccess(REGISTRATION_RESPONSE, MediaType.APPLICATION_JSON));

        PlatformDeployment saved = service.register(OPENID_CONFIGURATION, TOKEN);

        assertEquals(7L, saved.getKeyId());
        verify(toolDeploymentRepository, never()).save(any());
    }

    @Test
    public void testApprovedHostsAreAnApprovedCanvasAccountsHosts() {
        LmsApprovedAccount pending = LmsApprovedAccount.builder().name("Pending").lmsConnector(LmsConnector.CANVAS).approved(false).build();
        when(lmsApprovedAccountService.findByHost(LmsConnector.CANVAS, "canvas.iu.edu")).thenReturn(Optional.of(approvedAccount));
        when(lmsApprovedAccountService.findByHost(LmsConnector.CANVAS, "pending.instructure.com")).thenReturn(Optional.of(pending));
        when(lmsApprovedAccountService.findByHost(LmsConnector.CANVAS, "unknown.instructure.com")).thenReturn(Optional.empty());

        assertTrue(service.isApprovedHost("canvas.iu.edu"));
        assertFalse(service.isApprovedHost("pending.instructure.com"));
        assertFalse(service.isApprovedHost("unknown.instructure.com"));
    }

}
