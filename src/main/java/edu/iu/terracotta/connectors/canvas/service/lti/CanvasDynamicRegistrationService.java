package edu.iu.terracotta.connectors.canvas.service.lti;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;

/**
 * LTI Dynamic Registration from Canvas ("1.3 Dynamic Registration"). A Canvas whose address
 * belongs to an APPROVED account (lms_approved_account) can launch Terracotta right away; one whose
 * address no account has gets a PENDING account, and its launches wait until Terracotta staff
 * approve it. A REJECTED or REVOKED account can't register.
 */
public interface CanvasDynamicRegistrationService {

    /**
     * Registers Terracotta with the Canvas that sent the admin here: reads its OpenID
     * configuration, sends Terracotta's configuration (see CanvasToolConfigurationService), and
     * saves the platform deployment and tool deployment Canvas created, linked to the Canvas's
     * account (a new PENDING one if its address has none).
     *
     * @param openidConfiguration the openid_configuration URL Canvas sent
     * @param registrationToken the registration_token Canvas sent, good for this registration only
     * @return the saved platform deployment; its account says whether it can launch yet
     * @throws CanvasRegistrationException when the account is REJECTED or REVOKED, or the registration fails
     */
    PlatformDeployment register(String openidConfiguration, String registrationToken) throws CanvasRegistrationException;

    /**
     * Whether a Canvas host (e.g. iu.instructure.com) belongs to an approved account.
     */
    boolean isApprovedHost(String host);

}
