package edu.iu.terracotta.controller.lti;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import edu.iu.terracotta.connectors.canvas.service.lti.CanvasDynamicRegistrationService;
import edu.iu.terracotta.connectors.canvas.service.lti.CanvasRegistrationException;
import edu.iu.terracotta.connectors.canvas.service.lti.CanvasToolConfigurationService;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsApprovedAccountStatus;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;

public class CanvasLtiControllerTest {

    private CanvasToolConfigurationService canvasToolConfigurationService;
    private CanvasDynamicRegistrationService canvasDynamicRegistrationService;
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        canvasToolConfigurationService = mock(CanvasToolConfigurationService.class);
        canvasDynamicRegistrationService = mock(CanvasDynamicRegistrationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CanvasLtiController(canvasToolConfigurationService, canvasDynamicRegistrationService)).build();
    }

    @Test
    public void testServesTheDeveloperKeyConfigurationAsJson() throws Exception {
        when(canvasToolConfigurationService.developerKeyConfiguration()).thenReturn(Map.of("title", "Terracotta"));

        mockMvc.perform(get("/lti/canvas/config.json"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("Terracotta"));
    }

    private static PlatformDeployment registeredFor(boolean approved) {
        PlatformDeployment platformDeployment = new PlatformDeployment();
        platformDeployment.setLmsApprovedAccount(LmsApprovedAccount.builder().name("IU").lmsConnector(LmsConnector.CANVAS).status(approved ? LmsApprovedAccountStatus.APPROVED : LmsApprovedAccountStatus.PENDING).build());

        return platformDeployment;
    }

    @Test
    public void testShowsASuccessfulRegistration() throws Exception {
        when(canvasDynamicRegistrationService.register("https://iu.instructure.com/x", "t")).thenReturn(registeredFor(true));

        mockMvc.perform(get("/lti/canvas/registration").param("openid_configuration", "https://iu.instructure.com/x").param("registration_token", "t"))
            .andExpect(status().isOk())
            .andExpect(view().name(CanvasLtiController.REGISTRATION_TEMPLATE))
            .andExpect(model().attribute("registered", true))
            .andExpect(model().attribute("approved", true));
    }

    @Test
    public void testShowsARegistrationWaitingForApproval() throws Exception {
        when(canvasDynamicRegistrationService.register("https://new.instructure.com/x", "t")).thenReturn(registeredFor(false));

        mockMvc.perform(get("/lti/canvas/registration").param("openid_configuration", "https://new.instructure.com/x").param("registration_token", "t"))
            .andExpect(model().attribute("registered", true))
            .andExpect(model().attribute("approved", false));
    }

    @Test
    public void testShowsWhyARegistrationFailed() throws Exception {
        when(canvasDynamicRegistrationService.register("https://other.instructure.com/x", "t"))
            .thenThrow(new CanvasRegistrationException("other.instructure.com isn't approved to register Terracotta."));

        mockMvc.perform(get("/lti/canvas/registration").param("openid_configuration", "https://other.instructure.com/x").param("registration_token", "t"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("registered", false))
            .andExpect(model().attribute("errorMessage", "other.instructure.com isn't approved to register Terracotta."));
    }

    // the real page, rendered, so a template error can't wait until an admin registers
    private String render(boolean registered, String errorMessage) {
        return render(registered, true, errorMessage);
    }

    private String render(boolean registered, boolean approved, String errorMessage) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        Context context = new Context();
        context.setVariable("registered", registered);
        context.setVariable("approved", approved);
        context.setVariable("errorMessage", errorMessage);

        return engine.process(CanvasLtiController.REGISTRATION_TEMPLATE, context);
    }

    @Test
    public void testTheRegisteredPageTellsCanvasItsDone() {
        String page = render(true, null);

        assertTrue(page.contains("<title>Terracotta is registered</title>"), page);
        assertTrue(page.contains("access to the Canvas API"), page);
        assertTrue(page.contains("org.imsglobal.lti.close"), page);
        assertFalse(page.contains("couldn't be registered"), page);
    }

    @Test
    public void testTheWaitingPageSaysItNeedsApproval() {
        String page = render(true, false, null);

        assertTrue(page.contains("<title>Terracotta is waiting for approval</title>"), page);
        assertTrue(page.contains("can&#39;t be used until the Terracotta team approves your school") || page.contains("can't be used until the Terracotta team approves your school"), page);
        assertTrue(page.contains("org.imsglobal.lti.close"), page);
        assertFalse(page.contains("Terracotta is registered</h1>"), page);
    }

    @Test
    public void testTheErrorPageShowsTheReasonEscaped() {
        String page = render(false, "<b>other.instructure.com</b> isn't approved");

        assertTrue(page.contains("<title>Terracotta couldn&#39;t be registered</title>") || page.contains("<title>Terracotta couldn't be registered</title>"), page);
        assertTrue(page.contains("&lt;b&gt;other.instructure.com&lt;/b&gt;"), page);
        assertFalse(page.contains("Terracotta is registered"), page);
    }

}
