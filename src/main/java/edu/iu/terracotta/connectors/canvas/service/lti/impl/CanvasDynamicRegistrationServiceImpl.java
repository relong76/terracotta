package edu.iu.terracotta.connectors.canvas.service.lti.impl;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import edu.iu.terracotta.connectors.canvas.service.lti.CanvasDynamicRegistrationService;
import edu.iu.terracotta.connectors.canvas.service.lti.CanvasRegistrationException;
import edu.iu.terracotta.connectors.canvas.service.lti.CanvasToolConfigurationService;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.ToolDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.PlatformRegistrationDto;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.PlatformDeploymentRepository;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.ToolDeploymentRepository;
import edu.iu.terracotta.service.app.OutboundUrlService;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@SuppressWarnings("PMD.GuardLogStatement")
public class CanvasDynamicRegistrationServiceImpl implements CanvasDynamicRegistrationService {

    private static final String TOOL_CONFIGURATION = "https://purl.imsglobal.org/spec/lti-tool-configuration";
    private static final int TIMEOUT_MS = 15_000;

    private final CanvasToolConfigurationService canvasToolConfigurationService;
    private final OutboundUrlService outboundUrlService;
    private final PlatformDeploymentRepository platformDeploymentRepository;
    private final ToolDeploymentRepository toolDeploymentRepository;
    private final String applicationUrl;

    // not final, so tests can put a mock server behind it
    private RestTemplate restTemplate;

    public CanvasDynamicRegistrationServiceImpl(
            CanvasToolConfigurationService canvasToolConfigurationService,
            OutboundUrlService outboundUrlService,
            PlatformDeploymentRepository platformDeploymentRepository,
            ToolDeploymentRepository toolDeploymentRepository,
            @Value("${application.url}") String applicationUrl) {
        this.canvasToolConfigurationService = canvasToolConfigurationService;
        this.outboundUrlService = outboundUrlService;
        this.platformDeploymentRepository = platformDeploymentRepository;
        this.toolDeploymentRepository = toolDeploymentRepository;
        this.applicationUrl = Strings.CS.removeEnd(applicationUrl, "/");

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_MS);
        requestFactory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    // approved: Terracotta already has this Canvas in iss_configuration, by the address its API calls
    // go to (base_url). Not by issuer: every Canvas cloud instance shares https://canvas.instructure.com
    @Override
    public boolean isApprovedHost(String host) {
        if (StringUtils.isBlank(host)) {
            return false;
        }

        return platformDeploymentRepository.findAllByLmsConnector(LmsConnector.CANVAS).stream()
            .map(PlatformDeployment::getBaseUrl)
            .map(CanvasDynamicRegistrationServiceImpl::httpsHost)
            .anyMatch(baseUrlHost -> Strings.CI.equals(baseUrlHost, host));
    }

    @Override
    @Transactional
    public PlatformDeployment register(String openidConfiguration, String registrationToken) throws CanvasRegistrationException {
        if (StringUtils.isAnyBlank(openidConfiguration, registrationToken)) {
            throw new CanvasRegistrationException("This page must be opened from Canvas, as part of registering Terracotta.");
        }

        // the Canvas instance is the host the admin is registering from, e.g. iu.instructure.com
        // (Canvas cloud instances all share the issuer https://canvas.instructure.com)
        String canvasHost = httpsHost(openidConfiguration);

        if (canvasHost == null) {
            throw new CanvasRegistrationException("Canvas sent an invalid registration address.");
        }

        if (!isApprovedHost(canvasHost)) {
            log.warn("Refused a Canvas Dynamic Registration from [{}]: no Canvas platform deployment has that base URL", canvasHost);
            throw new CanvasRegistrationException(String.format("%s isn't approved to register Terracotta. Contact info@terracotta.education to request access.", canvasHost));
        }

        PlatformRegistrationDto platform = fetchPlatformConfiguration(openidConfiguration, registrationToken, canvasHost);
        JsonNode registration = sendRegistration(platform.getRegistration_endpoint(), registrationToken, canvasHost);

        return save(platform, registration, canvasHost);
    }

    private PlatformRegistrationDto fetchPlatformConfiguration(String openidConfiguration, String registrationToken, String canvasHost) throws CanvasRegistrationException {
        PlatformRegistrationDto platform;

        try {
            outboundUrlService.validatePublicUrl(openidConfiguration);
            // deepcode ignore Ssrf: the host is on the approved list, and validatePublicUrl rejects internal addresses
            platform = restTemplate.exchange(openidConfiguration, HttpMethod.GET, new HttpEntity<>(bearer(registrationToken)), PlatformRegistrationDto.class).getBody();
        } catch (IllegalArgumentException | RestClientException e) {
            log.error("Couldn't read the OpenID configuration of Canvas [{}]", canvasHost, e);
            throw new CanvasRegistrationException("Terracotta couldn't read this Canvas's registration details. Please try again.", e);
        }

        if (platform == null || StringUtils.isAnyBlank(platform.getIssuer(), platform.getAuthorization_endpoint(), platform.getJwks_uri(), platform.getToken_endpoint(), platform.getRegistration_endpoint())) {
            log.error("The OpenID configuration of Canvas [{}] is missing a required value", canvasHost);
            throw new CanvasRegistrationException("This Canvas's registration details are incomplete.");
        }

        // the registration goes back to the same Canvas, not wherever the configuration points
        if (!Strings.CI.equals(httpsHost(platform.getRegistration_endpoint()), canvasHost)) {
            log.error("Canvas [{}] gave a registration endpoint on another host: [{}]", canvasHost, platform.getRegistration_endpoint());
            throw new CanvasRegistrationException("This Canvas's registration details point somewhere else.");
        }

        if (platform.getPlatformConfiguration() != null && !Strings.CI.equals(platform.getPlatformConfiguration().getProduct_family_code(), "canvas")) {
            log.error("Platform [{}] isn't Canvas: [{}]", canvasHost, platform.getPlatformConfiguration().getProduct_family_code());
            throw new CanvasRegistrationException("Only Canvas can register Terracotta here.");
        }

        return platform;
    }

    private JsonNode sendRegistration(String registrationEndpoint, String registrationToken, String canvasHost) throws CanvasRegistrationException {
        JsonNode registration;

        try {
            outboundUrlService.validatePublicUrl(registrationEndpoint);
            HttpHeaders headers = bearer(registrationToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            // deepcode ignore Ssrf: same approved host as the OpenID configuration, checked above
            registration = restTemplate.exchange(registrationEndpoint, HttpMethod.POST, new HttpEntity<>(canvasToolConfigurationService.registrationRequest(), headers), JsonNode.class).getBody();
        } catch (IllegalArgumentException | RestClientException e) {
            log.error("Canvas [{}] didn't accept Terracotta's registration", canvasHost, e);
            throw new CanvasRegistrationException("Canvas didn't accept Terracotta's registration. Please try again, or contact info@terracotta.education.", e);
        }

        if (registration == null || StringUtils.isBlank(registration.path("client_id").asString(null))) {
            log.error("Canvas [{}] registered Terracotta without returning a client ID", canvasHost);
            throw new CanvasRegistrationException("Canvas didn't return the details Terracotta needs. Please contact info@terracotta.education.");
        }

        return registration;
    }

    private PlatformDeployment save(PlatformRegistrationDto platform, JsonNode registration, String canvasHost) {
        String clientId = registration.path("client_id").asString();
        String ltiDeploymentId = registration.path(TOOL_CONFIGURATION).path("deployment_id").asString(null);

        // registering again with the same client ID updates that record instead of adding another
        List<PlatformDeployment> existing = platformDeploymentRepository.findByIssAndClientId(platform.getIssuer(), clientId);
        PlatformDeployment platformDeployment = CollectionUtils.isNotEmpty(existing) ? existing.get(0) : new PlatformDeployment();

        platformDeployment.setIss(platform.getIssuer());
        platformDeployment.setClientId(clientId);
        platformDeployment.setOidcEndpoint(platform.getAuthorization_endpoint());
        platformDeployment.setJwksEndpoint(platform.getJwks_uri());
        platformDeployment.setOAuth2TokenUrl(platform.getToken_endpoint());
        // Canvas's REST API, for this instance
        platformDeployment.setBaseUrl("https://" + canvasHost);
        platformDeployment.setLocalUrl(applicationUrl);
        platformDeployment.setLmsConnector(LmsConnector.CANVAS);
        // installs in more subaccounts or courses add deployment IDs of their own on first launch
        platformDeployment.setEnableAutomaticDeployments(true);
        platformDeployment = platformDeploymentRepository.save(platformDeployment);

        if (StringUtils.isNotBlank(ltiDeploymentId)
                && CollectionUtils.isEmpty(toolDeploymentRepository.findByPlatformDeployment_IssAndPlatformDeployment_ClientIdAndLtiDeploymentId(platform.getIssuer(), clientId, ltiDeploymentId))) {
            ToolDeployment toolDeployment = new ToolDeployment();
            toolDeployment.setLtiDeploymentId(ltiDeploymentId);
            toolDeployment.setPlatformDeployment(platformDeployment);
            toolDeploymentRepository.save(toolDeployment);
        }

        log.info(
            "Canvas [{}] registered Terracotta: platform deployment ID: [{}], issuer: [{}], client ID: [{}], deployment ID: [{}]",
            canvasHost,
            platformDeployment.getKeyId(),
            platform.getIssuer(),
            clientId,
            ltiDeploymentId
        );

        return platformDeployment;
    }

    private static HttpHeaders bearer(String registrationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(registrationToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }

    // the host of an https URL, or null for anything else
    private static String httpsHost(String url) {
        try {
            URI uri = URI.create(url);

            return "https".equalsIgnoreCase(uri.getScheme()) && StringUtils.isNotBlank(uri.getHost()) ? uri.getHost().toLowerCase(Locale.ROOT) : null;
        } catch (IllegalArgumentException | NullPointerException _) {
            return null;
        }
    }

}
