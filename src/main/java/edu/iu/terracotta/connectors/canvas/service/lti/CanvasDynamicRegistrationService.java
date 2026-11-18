package edu.iu.terracotta.connectors.canvas.service.lti;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;

/**
 * LTI Dynamic Registration from Canvas ("1.3 Dynamic Registration"), for approved Canvas
 * instances only (terracotta.canvas.registration.approved-hosts).
 */
public interface CanvasDynamicRegistrationService {

    /**
     * Registers Terracotta with the Canvas that sent the admin here: reads its OpenID
     * configuration, sends Terracotta's configuration (see CanvasToolConfigurationService), and
     * saves the platform deployment and tool deployment Canvas created, so launches work right away.
     *
     * @param openidConfiguration the openid_configuration URL Canvas sent
     * @param registrationToken the registration_token Canvas sent, good for this registration only
     * @return the saved platform deployment
     * @throws CanvasRegistrationException when the Canvas isn't approved, or the registration fails
     */
    PlatformDeployment register(String openidConfiguration, String registrationToken) throws CanvasRegistrationException;

    /**
     * Whether a Canvas host (e.g. iu.instructure.com) may register Terracotta.
     */
    boolean isApprovedHost(String host);

}
