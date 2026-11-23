package edu.iu.terracotta.connectors.generic.dao.model.lti.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import edu.iu.terracotta.utils.LtiStrings;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginInitiationDto {

    private String iss;
    private String loginHint;
    private String targetLinkUri;
    private String ltiMessageHint;
    private String clientId;
    private String deploymentId;

    public LoginInitiationDto(HttpServletRequest req) {
        this(
            req.getParameter(LtiStrings.OIDC_ISS),
            req.getParameter(LtiStrings.OIDC_LOGIN_HINT),
            req.getParameter(LtiStrings.OIDC_TARGET_LINK_URI),
            req.getParameter(LtiStrings.OIDC_LTI_MESSAGE_HINT),
            req.getParameter(LtiStrings.OIDC_CLIENT_ID),
            req.getParameter(LtiStrings.OIDC_LTI_DEPLOYMENT_ID)
        );
    }

}
