package edu.iu.terracotta.connectors.canvas.service.lti.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class CanvasToolConfigurationServiceImplTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    // the developer key Terracotta QA runs on, from Canvas (nulls and empty values left out)
    private static final String QA_DEVELOPER_KEY = """
        {
          "title": "Terracotta QA (DEV4)",
          "description": "The terracotta tool (DEV4)",
          "target_link_uri": "https://qa.terracotta.education/lti3",
          "oidc_initiation_url": "https://qa.terracotta.education/oidc/login_initiations",
          "public_jwk_url": "https://qa.terracotta.education/jwks/jwk",
          "custom_fields": {
            "due_at": "$Canvas.assignment.dueAt.iso8601",
            "lock_at": "$Canvas.assignment.lockAt.iso8601",
            "unlock_at": "$Canvas.assignment.unlockAt.iso8601",
            "canvas_user_id": "$Canvas.user.id",
            "canvas_login_id": "$Canvas.user.loginId",
            "allowed_attempts": "$Canvas.assignment.allowedAttempts",
            "canvas_course_id": "$Canvas.course.id",
            "canvas_user_name": "$User.username",
            "student_attempts": "$Canvas.assignment.submission.studentAttempts",
            "canvas_assignment_id": "$Canvas.assignment.id",
            "canvas_user_global_id": "$Canvas.user.globalId"
          },
          "scopes": [
            "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly",
            "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
            "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly",
            "https://purl.imsglobal.org/spec/lti-ags/scope/score",
            "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly",
            "https://canvas.instructure.com/lti/account_lookup/scope/show",
            "https://purl.imsglobal.org/spec/lti/scope/noticehandlers"
          ],
          "extensions": [
            {
              "privacy_level": "public",
              "platform": "canvas.instructure.com",
              "settings": {
                "platform": "canvas.instructure.com",
                "placements": [
                  {
                    "enabled": true,
                    "placement": "course_navigation",
                    "message_type": "LtiResourceLinkRequest"
                  }
                ]
              }
            }
          ]
        }
        """;

    private CanvasToolConfigurationServiceImpl service;

    @BeforeEach
    public void beforeEach() {
        service = new CanvasToolConfigurationServiceImpl();
        // a trailing slash, as some configurations have it, shouldn't double up in the URLs
        ReflectionTestUtils.setField(service, "applicationUrl", "https://qa.terracotta.education/");
        ReflectionTestUtils.setField(service, "title", "Terracotta QA (DEV4)");
        ReflectionTestUtils.setField(service, "description", "The terracotta tool (DEV4)");
    }

    @Test
    public void testDeveloperKeyConfigurationMatchesTheQaDeveloperKey() throws Exception {
        JsonNode generated = MAPPER.valueToTree(service.developerKeyConfiguration());

        assertEquals(MAPPER.readTree(QA_DEVELOPER_KEY), generated);
    }

    @Test
    public void testCustomFieldsKeepCanvassOrder() {
        @SuppressWarnings("unchecked")
        Map<String, String> customFields = (Map<String, String>) service.developerKeyConfiguration().get("custom_fields");

        assertEquals(
            List.of("due_at", "lock_at", "unlock_at", "canvas_user_id", "canvas_login_id", "allowed_attempts", "canvas_course_id", "canvas_user_name", "student_attempts", "canvas_assignment_id", "canvas_user_global_id"),
            List.copyOf(customFields.keySet())
        );
    }

    @Test
    public void testRegistrationRequestDescribesTheSameToolForDynamicRegistration() {
        JsonNode registration = MAPPER.valueToTree(service.registrationRequest());
        JsonNode toolConfiguration = registration.path("https://purl.imsglobal.org/spec/lti-tool-configuration");
        JsonNode qa = MAPPER.readTree(QA_DEVELOPER_KEY);

        assertEquals("web", registration.path("application_type").asString());
        assertEquals("Terracotta QA (DEV4)", registration.path("client_name").asString());
        assertEquals("https://qa.terracotta.education/oidc/login_initiations", registration.path("initiate_login_uri").asString());
        assertEquals("https://qa.terracotta.education/jwks/jwk", registration.path("jwks_uri").asString());
        assertEquals("https://qa.terracotta.education/lti3", registration.path("redirect_uris").path(0).asString());
        assertEquals("private_key_jwt", registration.path("token_endpoint_auth_method").asString());
        assertEquals(MAPPER.valueToTree(List.of("client_credentials", "implicit")), registration.path("grant_types"));
        assertEquals(MAPPER.valueToTree(List.of("id_token")), registration.path("response_types"));

        // one space separated string, with the developer key's scopes
        assertEquals(String.join(" ", MAPPER.convertValue(qa.path("scopes"), String[].class)), registration.path("scope").asString());

        assertEquals("qa.terracotta.education", toolConfiguration.path("domain").asString());
        assertEquals("https://qa.terracotta.education/lti3", toolConfiguration.path("target_link_uri").asString());
        assertEquals(qa.path("custom_fields"), toolConfiguration.path("custom_parameters"));
        assertEquals("public", toolConfiguration.path("https://canvas.instructure.com/lti/privacy_level").asString());
        assertEquals("LtiResourceLinkRequest", toolConfiguration.path("messages").path(0).path("type").asString());
        assertEquals("course_navigation", toolConfiguration.path("messages").path(0).path("placements").path(0).asString());
        assertEquals("https://qa.terracotta.education/lti3", toolConfiguration.path("messages").path(0).path("target_link_uri").asString());
        assertFalse(toolConfiguration.has("messages_supported"));
    }

}
