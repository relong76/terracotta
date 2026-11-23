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
import edu.iu.terracotta.utils.LtiStrings;

@Service
public class CanvasToolConfigurationServiceImpl implements CanvasToolConfigurationService {

    public static final String LAUNCH_PATH = "/lti3";
    public static final String LOGIN_INITIATION_PATH = "/oidc/login_initiations";
    public static final String JWKS_PATH = "/jwks/jwk";
    public static final String CANVAS_PLATFORM = LtiStrings.CANVAS_PLATFORM;
    public static final String PLACEMENT = LtiStrings.CANVAS_PLACEMENT_COURSE_NAVIGATION;
    public static final String MESSAGE_TYPE = LtiStrings.LTI_MESSAGE_TYPE_RESOURCE_LINK;
    // names, emails and the Canvas IDs below reach Terracotta at launch
    public static final String PRIVACY_LEVEL = LtiStrings.CANVAS_PRIVACY_LEVEL_PUBLIC;

    // what Terracotta reads from each launch (see LtiStrings and the launch handling)
    public static final Map<String, String> CUSTOM_FIELDS = customFields();

    public static final List<String> SCOPES = List.of(
        LtiStrings.LTI_SCOPE_AGS_LINEITEM_READONLY,
        LtiStrings.LTI_SCOPE_AGS_LINEITEM,
        LtiStrings.LTI_SCOPE_AGS_RESULT_READONLY,
        LtiStrings.LTI_SCOPE_AGS_SCORE,
        LtiStrings.LTI_SCOPE_NRPS_MEMBERSHIP_READONLY,
        LtiStrings.CANVAS_SCOPE_ACCOUNT_LOOKUP,
        // course copy notices (see NoticeController)
        LtiStrings.LTI_SCOPE_NOTICE_HANDLERS
    );

    // the OpenID claims a launch carries; Canvas's privacy level decides which it actually sends
    public static final List<String> CLAIMS = List.of(LtiStrings.ISS, LtiStrings.SUB, LtiStrings.LTI_NAME, LtiStrings.LTI_GIVEN_NAME, LtiStrings.LTI_FAMILY_NAME, LtiStrings.LTI_EMAIL);

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
        message.put(LtiStrings.TOOL_MESSAGE_TYPE, MESSAGE_TYPE);
        message.put(LtiStrings.TOOL_MESSAGE_TARGET_LINK_URI, url(LAUNCH_PATH));
        message.put(LtiStrings.TOOL_MESSAGE_LABEL, title);
        message.put(LtiStrings.TOOL_MESSAGE_PLACEMENTS, List.of(PLACEMENT));

        Map<String, Object> toolConfiguration = new LinkedHashMap<>();
        toolConfiguration.put(LtiStrings.TOOL_CONFIGURATION_DOMAIN, URI.create(baseUrl()).getHost());
        toolConfiguration.put(LtiStrings.TOOL_CONFIGURATION_DESCRIPTION, description);
        toolConfiguration.put(LtiStrings.TOOL_CONFIGURATION_TARGET_LINK_URI, url(LAUNCH_PATH));
        toolConfiguration.put(LtiStrings.TOOL_CONFIGURATION_CUSTOM_PARAMETERS, CUSTOM_FIELDS);
        toolConfiguration.put(LtiStrings.TOOL_CONFIGURATION_CLAIMS, CLAIMS);
        toolConfiguration.put(LtiStrings.TOOL_CONFIGURATION_MESSAGES, List.of(message));
        toolConfiguration.put(LtiStrings.CANVAS_PRIVACY_LEVEL, PRIVACY_LEVEL);

        Map<String, Object> registration = new LinkedHashMap<>();
        registration.put(LtiStrings.REGISTRATION_APPLICATION_TYPE, LtiStrings.REGISTRATION_APPLICATION_TYPE_WEB);
        registration.put(LtiStrings.REGISTRATION_CLIENT_NAME, title);
        registration.put(LtiStrings.REGISTRATION_GRANT_TYPES, List.of(LtiStrings.OAUTH_GRANT_TYPE_CLIENT_CREDENTIALS, LtiStrings.OAUTH_GRANT_TYPE_IMPLICIT));
        registration.put(LtiStrings.REGISTRATION_RESPONSE_TYPES, List.of(LtiStrings.OIDC_ID_TOKEN));
        registration.put(LtiStrings.REGISTRATION_REDIRECT_URIS, List.of(url(LAUNCH_PATH)));
        registration.put(LtiStrings.REGISTRATION_INITIATE_LOGIN_URI, url(LOGIN_INITIATION_PATH));
        registration.put(LtiStrings.REGISTRATION_JWKS_URI, url(JWKS_PATH));
        registration.put(LtiStrings.REGISTRATION_TOKEN_ENDPOINT_AUTH_METHOD, LtiStrings.OAUTH_TOKEN_ENDPOINT_AUTH_PRIVATE_KEY_JWT);
        // one space separated string, as the spec (OAuth 2.0 dynamic client registration) has it
        registration.put(LtiStrings.REGISTRATION_SCOPE, String.join(" ", SCOPES));
        registration.put(LtiStrings.REGISTRATION_TOOL_CONFIGURATION, toolConfiguration);

        return registration;
    }

    private String baseUrl() {
        return Strings.CS.removeEnd(applicationUrl, "/");
    }

    private String url(String path) {
        return baseUrl() + path;
    }

}
