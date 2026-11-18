package edu.iu.terracotta.connectors.canvas.service.lti.impl;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.iu.terracotta.connectors.canvas.service.lti.CanvasToolConfigurationService;

@Service
public class CanvasToolConfigurationServiceImpl implements CanvasToolConfigurationService {

    public static final String LAUNCH_PATH = "/lti3";
    public static final String LOGIN_INITIATION_PATH = "/oidc/login_initiations";
    public static final String JWKS_PATH = "/jwks/jwk";
    public static final String CANVAS_PLATFORM = "canvas.instructure.com";
    public static final String PLACEMENT = "course_navigation";
    public static final String MESSAGE_TYPE = "LtiResourceLinkRequest";
    // names, emails and the Canvas IDs below reach Terracotta at launch
    public static final String PRIVACY_LEVEL = "public";

    // what Terracotta reads from each launch (see LtiStrings and the launch handling)
    public static final Map<String, String> CUSTOM_FIELDS = customFields();

    public static final List<String> SCOPES = List.of(
        "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly",
        "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
        "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly",
        "https://purl.imsglobal.org/spec/lti-ags/scope/score",
        "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly",
        "https://canvas.instructure.com/lti/account_lookup/scope/show",
        // course copy notices (see NoticeController)
        "https://purl.imsglobal.org/spec/lti/scope/noticehandlers"
    );

    // the OpenID claims a launch carries; Canvas's privacy level decides which it actually sends
    public static final List<String> CLAIMS = List.of("iss", "sub", "name", "given_name", "family_name", "email");

    @Value("${application.url}")
    private String applicationUrl;

    @Value("${application.name}")
    private String title;

    @Value("${application.description}")
    private String description;

    private static Map<String, String> customFields() {
        // the order Canvas shows them in
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("due_at", "$Canvas.assignment.dueAt.iso8601");
        fields.put("lock_at", "$Canvas.assignment.lockAt.iso8601");
        fields.put("unlock_at", "$Canvas.assignment.unlockAt.iso8601");
        fields.put("canvas_user_id", "$Canvas.user.id");
        fields.put("canvas_login_id", "$Canvas.user.loginId");
        fields.put("allowed_attempts", "$Canvas.assignment.allowedAttempts");
        fields.put("canvas_course_id", "$Canvas.course.id");
        fields.put("canvas_user_name", "$User.username");
        fields.put("student_attempts", "$Canvas.assignment.submission.studentAttempts");
        fields.put("canvas_assignment_id", "$Canvas.assignment.id");
        fields.put("canvas_user_global_id", "$Canvas.user.globalId");

        return Collections.unmodifiableMap(fields);
    }

    @Override
    public Map<String, Object> developerKeyConfiguration() {
        Map<String, Object> placement = new LinkedHashMap<>();
        placement.put("enabled", true);
        placement.put("placement", PLACEMENT);
        placement.put("message_type", MESSAGE_TYPE);

        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("platform", CANVAS_PLATFORM);
        settings.put("placements", List.of(placement));

        Map<String, Object> extension = new LinkedHashMap<>();
        extension.put("platform", CANVAS_PLATFORM);
        extension.put("privacy_level", PRIVACY_LEVEL);
        extension.put("settings", settings);

        Map<String, Object> configuration = new LinkedHashMap<>();
        configuration.put("title", title);
        configuration.put("description", description);
        configuration.put("target_link_uri", url(LAUNCH_PATH));
        configuration.put("oidc_initiation_url", url(LOGIN_INITIATION_PATH));
        configuration.put("public_jwk_url", url(JWKS_PATH));
        configuration.put("custom_fields", CUSTOM_FIELDS);
        configuration.put("scopes", SCOPES);
        configuration.put("extensions", List.of(extension));

        return configuration;
    }

    @Override
    public Map<String, Object> registrationRequest() {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", MESSAGE_TYPE);
        message.put("target_link_uri", url(LAUNCH_PATH));
        message.put("label", title);
        message.put("placements", List.of(PLACEMENT));

        Map<String, Object> toolConfiguration = new LinkedHashMap<>();
        toolConfiguration.put("domain", URI.create(baseUrl()).getHost());
        toolConfiguration.put("description", description);
        toolConfiguration.put("target_link_uri", url(LAUNCH_PATH));
        toolConfiguration.put("custom_parameters", CUSTOM_FIELDS);
        toolConfiguration.put("claims", CLAIMS);
        toolConfiguration.put("messages", List.of(message));
        toolConfiguration.put("https://canvas.instructure.com/lti/privacy_level", PRIVACY_LEVEL);

        Map<String, Object> registration = new LinkedHashMap<>();
        registration.put("application_type", "web");
        registration.put("client_name", title);
        registration.put("grant_types", List.of("client_credentials", "implicit"));
        registration.put("response_types", List.of("id_token"));
        registration.put("redirect_uris", List.of(url(LAUNCH_PATH)));
        registration.put("initiate_login_uri", url(LOGIN_INITIATION_PATH));
        registration.put("jwks_uri", url(JWKS_PATH));
        registration.put("token_endpoint_auth_method", "private_key_jwt");
        // one space separated string, as the spec (OAuth 2.0 dynamic client registration) has it
        registration.put("scope", String.join(" ", SCOPES));
        registration.put("https://purl.imsglobal.org/spec/lti-tool-configuration", toolConfiguration);

        return registration;
    }

    private String baseUrl() {
        return Strings.CS.removeEnd(applicationUrl, "/");
    }

    private String url(String path) {
        return baseUrl() + path;
    }

}
