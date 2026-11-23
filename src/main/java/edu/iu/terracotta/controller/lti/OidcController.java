package edu.iu.terracotta.controller.lti;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.ToolDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.LoginInitiationDto;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.PlatformDeploymentRepository;
import edu.iu.terracotta.connectors.generic.service.lti.LtiDataService;
import edu.iu.terracotta.dao.entity.LtiNonce;
import edu.iu.terracotta.dao.repository.LtiNonceRepository;
import edu.iu.terracotta.utils.LtiStrings;
import edu.iu.terracotta.utils.TextConstants;
import edu.iu.terracotta.utils.lti.LtiOidcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This LTI controller should be protected by OAuth 1.0a (on the /oauth path)
 * This will handle LTI 1 and 2 (many of the paths ONLY make sense for LTI2 though)
 * Sample Key "key" and secret "secret"
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/oidc/login_initiations")
public class OidcController {

    private final PlatformDeploymentRepository platformDeploymentRepository;
    private final LtiDataService ltiDataService;
    private final LtiNonceRepository ltiNonceRepository;

    @Value("${app.lti.data.verbose.logging.enabled:false}")
    private boolean ltiDataVerboseLoggingEnabled;

    /**
     * This will receive the request to start the OIDC process.
     * We receive some parameters (iss, login_hint, target_link_uri, lti_message_hint, and optionally, the deployment_id and the client_id)
     */
    @PostMapping
    public String loginInitiations(HttpServletRequest req, Model model) {
        // We need to receive the parameters and search for the deployment of the tool that matches with what we receive.
        LoginInitiationDto loginInitiationDTO = new LoginInitiationDto(req);
        List<PlatformDeployment> platformDeploymentListEntityList;
        // Getting the client_id (that is optional) and can come in the form or in the URL.
        String clientIdValue;

        // If we already have it in the loginInitiationDTO
        if (loginInitiationDTO.getClientId() != null) {
            clientIdValue = loginInitiationDTO.getClientId();
        } else {  // We try to get it from the URL query parameters.
            clientIdValue = req.getParameter(LtiStrings.OIDC_CLIENT_ID);
        }

        // Getting the deployment_id (that is optional) and can come in the form or in the URL.
        String deploymentIdValue;

        // If we already have it in the loginInitiationDTO
        if (loginInitiationDTO.getDeploymentId() != null) {
            deploymentIdValue = loginInitiationDTO.getDeploymentId();
        } else {  // We try to get it from the URL query getDeploymentId.
            deploymentIdValue = req.getParameter(LtiStrings.OIDC_LTI_DEPLOYMENT_ID);
        }

        // We search for the platformDeployment.
        // We will try all the options here (from more detailed to less), and we will deal with the error if there are more than one result.
        if (clientIdValue != null && deploymentIdValue != null) {
            // search for platformDeployment by iss, clientId and deploymentIdValue
            platformDeploymentListEntityList = platformDeploymentRepository.findByIssAndClientIdAndToolDeployments_LtiDeploymentId(loginInitiationDTO.getIss(), clientIdValue, deploymentIdValue);
            if (platformDeploymentListEntityList.isEmpty()) {
                // if missing, check if we can automatically create a ToolDeployment
                ToolDeployment toolDeployment = this.ltiDataService.findOrCreateToolDeployment(loginInitiationDTO.getIss(), clientIdValue, deploymentIdValue);
                if (toolDeployment != null) {
                    platformDeploymentListEntityList = Collections.singletonList(toolDeployment.getPlatformDeployment());
                }
            }
        } else if (clientIdValue != null) {
            platformDeploymentListEntityList = platformDeploymentRepository.findByIssAndClientId(loginInitiationDTO.getIss(), clientIdValue);
        } else if (deploymentIdValue != null) {
            platformDeploymentListEntityList = platformDeploymentRepository.findByIssAndToolDeployments_LtiDeploymentId(loginInitiationDTO.getIss(), deploymentIdValue);
        } else {
            platformDeploymentListEntityList = platformDeploymentRepository.findByIss(loginInitiationDTO.getIss());
        }

        // We deal with some possible errors
        if (platformDeploymentListEntityList.isEmpty()) {  //If we don't have configuration
            model.addAttribute(TextConstants.ERROR, "Not found any existing tool deployment with iss: " + loginInitiationDTO.getIss() +
                    " clientId: " + clientIdValue + " deploymentId: " + deploymentIdValue);

            return TextConstants.LTI3ERROR;
        }

        if (platformDeploymentListEntityList.size() > 1) {   // If we have more than one match.
            model.addAttribute(TextConstants.ERROR, "We have more than one tool deployment with iss: " + loginInitiationDTO.getIss() +
                    " clientId: " + clientIdValue + " deploymentId: " + deploymentIdValue);

            return TextConstants.LTI3ERROR;
        }

        // If we have arrived here, it means that we have only one result (as expected)
        PlatformDeployment lti3KeyEntity = platformDeploymentListEntityList.get(0);

        if (clientIdValue == null) {
            clientIdValue = lti3KeyEntity.getClientId();
        }

        try {
            // We are going to create the OIDC request,
            Map<String, String> parameters = generateAuthRequestPayload(lti3KeyEntity, loginInitiationDTO, clientIdValue, deploymentIdValue);

            // We add that information so the thymeleaf template can display it (and prepare the links)
            //model.addAllAttributes(parameters);
            // These 3 are to display what we received from the platform.
            if (ltiDataService.getDemoMode()) {
                model.addAllAttributes(parameters);
                model.addAttribute("initiation_dto", loginInitiationDTO);
                model.addAttribute("client_id_received", clientIdValue);
                model.addAttribute("deployment_id_received", deploymentIdValue);
            }

            // The state is a self-contained signed JWT (see LtiOidcUtils.generateState), so its own
            // signature is enough to validate it later - no need to track issued states ourselves.
            // The nonce isn't self-verifying, so it's persisted here and consumed exactly once
            // (Lti3Request.checkNonce) instead of being tracked in a session.
            ltiNonceRepository.save(LtiNonce.builder().nonce(parameters.get(LtiStrings.OIDC_NONCE)).build());

            // Once we have the data ready, we redirect straight to the platform's OIDC
            // authorization endpoint - the launch doesn't depend on cookies, so there's no need
            // to route through a storage-access check first.
            if (!ltiDataService.getDemoMode()) {
                // deepcode ignore OR: the redirect base is the OIDC endpoint stored on the matched platform deployment, and the request values are only appended as URL encoded query parameters
                return "redirect:" + parameters.get("oicdEndpointComplete");
            }

            return "oidcRedirect";
        } catch (Exception ex) {
            log.error("Failed creating OIDC request", ex);
            model.addAttribute(TextConstants.ERROR, ex.getMessage());

            return TextConstants.LTI3ERROR;
        }
    }

    /**
     * This generates a map with all the information that we need to send to the OIDC Authorization endpoint in the Platform.
     * In this case, we will put this in the model to be used by the thymeleaf template.
     */
    private Map<String, String> generateAuthRequestPayload(PlatformDeployment platformDeployment, LoginInitiationDto loginInitiationDto, String clientIdValue, String deploymentIdValue) throws GeneralSecurityException, IOException {
        Map<String, String> authRequestMap = new HashMap<>();
        authRequestMap.put(LtiStrings.OIDC_CLIENT_ID, platformDeployment.getClientId()); //As it came from the Platform (if it came... if not we should have it configured)
        authRequestMap.put(LtiStrings.OIDC_LOGIN_HINT, loginInitiationDto.getLoginHint()); //As it came from the Platform
        authRequestMap.put(LtiStrings.OIDC_LTI_MESSAGE_HINT, loginInitiationDto.getLtiMessageHint()); //As it came from the Platform
        String nonce = UUID.randomUUID().toString(); // We generate a nonce to allow this auth request to be used only one time.
        authRequestMap.put(LtiStrings.OIDC_NONCE, nonce);  //The nonce
        authRequestMap.put(LtiStrings.OIDC_PROMPT, LtiStrings.OIDC_PROMPT_NONE);  //Always this value, as specified in the standard.
        authRequestMap.put(LtiStrings.OIDC_REDIRECT_URI, String.format("%s/lti3", platformDeployment.getLocalUrl()));
        authRequestMap.put(LtiStrings.OIDC_RESPONSE_MODE, LtiStrings.OIDC_RESPONSE_MODE_FORM_POST); //Always this value, as specified in the standard.
        authRequestMap.put(LtiStrings.OIDC_RESPONSE_TYPE, LtiStrings.OIDC_ID_TOKEN); //Always this value, as specified in the standard.
        authRequestMap.put(LtiStrings.OIDC_SCOPE, LtiStrings.OIDC_SCOPE_OPENID);  //Always this value, as specified in the standard.
        // The state is something that we can create and add anything we want on it.
        // On this case, we have decided to create a JWT token with some information that we will use as additional security. But it is not mandatory.
        String state = LtiOidcUtils.generateState(ltiDataService, platformDeployment, authRequestMap, loginInitiationDto, clientIdValue, deploymentIdValue, ltiDataVerboseLoggingEnabled);
        authRequestMap.put(LtiStrings.OIDC_STATE, state); //The state we use later to retrieve some useful information about the OICD request.
        authRequestMap.put("oicdEndpoint", platformDeployment.getOidcEndpoint());  //We need this in the Thymeleaf template in case we decide to use the POST method. It is the endpoint where the LMS receives the OICD requests
        authRequestMap.put("oicdEndpointComplete", generateCompleteUrl(authRequestMap));  //This generates the URL to use in case we decide to use the GET method

        return authRequestMap;
    }

    /**
     * This generates the GET URL with all the query string parameters.
     */
    private String generateCompleteUrl(Map<String, String> model) throws UnsupportedEncodingException {
        StringBuilder getUrl = new StringBuilder();

        getUrl.append(model.get("oicdEndpoint"));
        getUrl = addParameter(getUrl, LtiStrings.OIDC_CLIENT_ID, model.get(LtiStrings.OIDC_CLIENT_ID), true);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_LOGIN_HINT, model.get(LtiStrings.OIDC_LOGIN_HINT), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_LTI_MESSAGE_HINT, model.get(LtiStrings.OIDC_LTI_MESSAGE_HINT), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_NONCE, model.get(LtiStrings.OIDC_NONCE), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_PROMPT, model.get(LtiStrings.OIDC_PROMPT), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_REDIRECT_URI, model.get(LtiStrings.OIDC_REDIRECT_URI), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_RESPONSE_MODE, model.get(LtiStrings.OIDC_RESPONSE_MODE), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_RESPONSE_TYPE, model.get(LtiStrings.OIDC_RESPONSE_TYPE), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_SCOPE, model.get(LtiStrings.OIDC_SCOPE), false);
        getUrl = addParameter(getUrl, LtiStrings.OIDC_STATE, model.get(LtiStrings.OIDC_STATE), false);

        return getUrl.toString();
    }

    private StringBuilder addParameter(StringBuilder url, String parameter, String value, boolean first) throws UnsupportedEncodingException {
        if (value != null) {
            if (first) {
                url.append("?").append(parameter).append("=");
            } else {
                url.append("&").append(parameter).append("=");
            }

            url.append(URLEncoder.encode(value, String.valueOf(StandardCharsets.UTF_8)));
        }

        return url;
    }

}
