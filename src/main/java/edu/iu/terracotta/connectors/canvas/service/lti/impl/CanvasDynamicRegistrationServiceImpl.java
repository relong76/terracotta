package edu.iu.terracotta.connectors.canvas.service.lti.impl;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

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
import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.ToolDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsApprovedAccountStatus;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.PlatformConfigurationDto;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.PlatformRegistrationDto;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.PlatformDeploymentRepository;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.ToolDeploymentRepository;
import edu.iu.terracotta.connectors.generic.service.lti.LmsApprovedAccountService;
import edu.iu.terracotta.service.app.OutboundUrlService;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@SuppressWarnings("PMD.GuardLogStatement")
public class CanvasDynamicRegistrationServiceImpl implements CanvasDynamicRegistrationService {

    private static final String TOOL_CONFIGURATION = "https://purl.imsglobal.org/spec/lti-tool-configuration";
    private static final int TIMEOUT_MS = 15_000;

    // where Canvas cloud schools start a registration, and where Canvas cloud takes it
    static final Set<String> CANVAS_CLOUD_CONFIGURATION_HOSTS = Set.of("canvas.instructure.com", "canvas.beta.instructure.com", "canvas.test.instructure.com");
    static final Set<String> CANVAS_CLOUD_REGISTRATION_HOSTS = Set.of(
        "sso.canvaslms.com", "sso.beta.canvaslms.com", "sso.test.canvaslms.com",
        "canvas.instructure.com", "canvas.beta.instructure.com", "canvas.test.instructure.com"
    );

    // the school registering: its Canvas address, and (Canvas cloud only) its account ID and name
    private record CanvasAccount(String host, String platformGuid, String name) {}

    private final CanvasToolConfigurationService canvasToolConfigurationService;
    private final OutboundUrlService outboundUrlService;
    private final PlatformDeploymentRepository platformDeploymentRepository;
    private final ToolDeploymentRepository toolDeploymentRepository;
    private final LmsApprovedAccountService lmsApprovedAccountService;
    private final String applicationUrl;

    // not final, so tests can put a mock server behind it
    private RestTemplate restTemplate;

    public CanvasDynamicRegistrationServiceImpl(
            CanvasToolConfigurationService canvasToolConfigurationService,
            OutboundUrlService outboundUrlService,
            PlatformDeploymentRepository platformDeploymentRepository,
            ToolDeploymentRepository toolDeploymentRepository,
            LmsApprovedAccountService lmsApprovedAccountService,
            @Value("${application.url}") String applicationUrl) {
        this.canvasToolConfigurationService = canvasToolConfigurationService;
        this.outboundUrlService = outboundUrlService;
        this.platformDeploymentRepository = platformDeploymentRepository;
        this.toolDeploymentRepository = toolDeploymentRepository;
        this.lmsApprovedAccountService = lmsApprovedAccountService;
        this.applicationUrl = Strings.CS.removeEnd(applicationUrl, "/");

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_MS);
        requestFactory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    @Transactional
    public PlatformDeployment register(String openidConfiguration, String registrationToken) throws CanvasRegistrationException {
        if (StringUtils.isAnyBlank(openidConfiguration, registrationToken)) {
            throw new CanvasRegistrationException("This page must be opened from Canvas, as part of registering Terracotta.");
        }

        // canvas.instructure.com for every Canvas cloud school; a self-hosted Canvas's own address
        String configurationHost = httpsHost(openidConfiguration);

        if (configurationHost == null) {
            throw new CanvasRegistrationException("Canvas sent an invalid registration address.");
        }

        PlatformRegistrationDto platform = fetchPlatformConfiguration(openidConfiguration, registrationToken, configurationHost);
        CanvasAccount canvasAccount = identify(platform, configurationHost);
        Optional<LmsApprovedAccount> existing;

        try {
            existing = lmsApprovedAccountService.find(LmsConnector.CANVAS, canvasAccount.host(), canvasAccount.platformGuid());
        } catch (IllegalStateException e) {
            log.error("Refused a Canvas Dynamic Registration from [{}]: {}", canvasAccount.host(), e.getMessage());
            throw new CanvasRegistrationException(String.format("%s can't register Terracotta. Contact info@terracotta.education for help.", canvasAccount.host()), e);
        }

        // a rejected or revoked account is turned away before the registration is sent
        if (existing.isPresent() && (existing.get().getStatus() == LmsApprovedAccountStatus.REJECTED || existing.get().getStatus() == LmsApprovedAccountStatus.REVOKED)) {
            log.warn("Refused a Canvas Dynamic Registration from [{}]: its account is {}", canvasAccount.host(), existing.get().getStatus());
            throw new CanvasRegistrationException(String.format("%s can't register Terracotta. Contact info@terracotta.education for help.", canvasAccount.host()));
        }

        JsonNode registration = sendRegistration(platform.getRegistration_endpoint(), registrationToken, canvasAccount.host());

        // a school no account has yet gets a PENDING one: the registration is kept, and its
        // launches wait until Terracotta staff approve the account
        LmsApprovedAccount account = lmsApprovedAccountService.findOrCreate(LmsConnector.CANVAS, canvasAccount.host(), canvasAccount.platformGuid(), canvasAccount.name());

        return save(platform, registration, canvasAccount.host(), account);
    }

    /**
     * The school registering. Canvas cloud schools all register through canvas.instructure.com,
     * whose configuration names the school: trusted, since it comes from Canvas cloud itself. A
     * self-hosted Canvas could say anything about itself, so it's only ever its own address.
     */
    private static CanvasAccount identify(PlatformRegistrationDto platform, String configurationHost) throws CanvasRegistrationException {
        PlatformConfigurationDto platformConfiguration = platform.getPlatformConfiguration();
        String accountDomain = platformConfiguration != null ? hostOf(platformConfiguration.getCanvasAccountDomain()) : null;
        String accountName = platformConfiguration != null ? StringUtils.trimToNull(platformConfiguration.getCanvasAccountName()) : null;

        if (CANVAS_CLOUD_CONFIGURATION_HOSTS.contains(configurationHost)) {
            if (accountDomain == null) {
                log.error("Canvas cloud [{}] didn't say which school is registering", configurationHost);
                throw new CanvasRegistrationException("Canvas didn't say which school is registering Terracotta. Please try again, or contact info@terracotta.education.");
            }

            return new CanvasAccount(accountDomain, StringUtils.trimToNull(platformConfiguration.getCanvasAccountLtiGuid()), accountName);
        }

        if (accountDomain != null && !accountDomain.equals(configurationHost)) {
            log.error("Self-hosted Canvas [{}] said it's [{}]", configurationHost, accountDomain);
            throw new CanvasRegistrationException("This Canvas's registration details point somewhere else.");
        }

        return new CanvasAccount(configurationHost, null, accountName);
    }

    private PlatformRegistrationDto fetchPlatformConfiguration(String openidConfiguration, String registrationToken, String configurationHost) throws CanvasRegistrationException {
        PlatformRegistrationDto platform;

        try {
            outboundUrlService.validatePublicUrl(openidConfiguration);
            // deepcode ignore Ssrf: openid_configuration comes from the request by design of LTI Dynamic Registration and is checked by validatePublicUrl above
            platform = restTemplate.exchange(openidConfiguration, HttpMethod.GET, new HttpEntity<>(bearer(registrationToken)), PlatformRegistrationDto.class).getBody();
        } catch (IllegalArgumentException | RestClientException e) {
            log.error("Couldn't read the OpenID configuration of Canvas [{}]", configurationHost, e);
            throw new CanvasRegistrationException("Terracotta couldn't read this Canvas's registration details. Please try again.", e);
        }

        if (platform == null || StringUtils.isAnyBlank(platform.getIssuer(), platform.getAuthorization_endpoint(), platform.getJwks_uri(), platform.getToken_endpoint(), platform.getRegistration_endpoint())) {
            log.error("The OpenID configuration of Canvas [{}] is missing a required value", configurationHost);
            throw new CanvasRegistrationException("This Canvas's registration details are incomplete.");
        }

        // the registration goes back to Canvas: Canvas cloud's own servers, or the self-hosted Canvas itself
        String registrationHost = httpsHost(platform.getRegistration_endpoint());
        boolean cloud = CANVAS_CLOUD_CONFIGURATION_HOSTS.contains(configurationHost);

        if (registrationHost == null || (cloud ? !CANVAS_CLOUD_REGISTRATION_HOSTS.contains(registrationHost) : !registrationHost.equals(configurationHost))) {
            log.error("Canvas [{}] gave a registration endpoint somewhere else: [{}]", configurationHost, platform.getRegistration_endpoint());
            throw new CanvasRegistrationException("This Canvas's registration details point somewhere else.");
        }

        if (platform.getPlatformConfiguration() != null && !Strings.CI.equals(platform.getPlatformConfiguration().getProduct_family_code(), "canvas")) {
            log.error("Platform [{}] isn't Canvas: [{}]", configurationHost, platform.getPlatformConfiguration().getProduct_family_code());
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

    private PlatformDeployment save(PlatformRegistrationDto platform, JsonNode registration, String canvasHost, LmsApprovedAccount account) {
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
        platformDeployment.setLmsApprovedAccount(account);
        platformDeployment = platformDeploymentRepository.save(platformDeployment);

        if (StringUtils.isNotBlank(ltiDeploymentId)
                && CollectionUtils.isEmpty(toolDeploymentRepository.findByPlatformDeployment_IssAndPlatformDeployment_ClientIdAndLtiDeploymentId(platform.getIssuer(), clientId, ltiDeploymentId))) {
            ToolDeployment toolDeployment = new ToolDeployment();
            toolDeployment.setLtiDeploymentId(ltiDeploymentId);
            toolDeployment.setPlatformDeployment(platformDeployment);
            toolDeploymentRepository.save(toolDeployment);
        }

        log.info(
            "Canvas [{}] registered Terracotta (account {}): platform deployment ID: [{}], issuer: [{}], client ID: [{}], deployment ID: [{}]",
            canvasHost,
            account.getStatus(),
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

    // a host name, lower case, from a host or URL Canvas gave
    private static String hostOf(String hostOrUrl) {
        if (StringUtils.isBlank(hostOrUrl)) {
            return null;
        }

        String value = hostOrUrl.strip();

        return httpsHost(Strings.CS.contains(value, "://") ? value : "https://" + value);
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
