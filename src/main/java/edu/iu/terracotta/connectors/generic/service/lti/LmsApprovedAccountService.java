package edu.iu.terracotta.connectors.generic.service.lti;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.model.lti.dto.LmsApprovedAccountDto;

/**
 * The LMS accounts that may use Terracotta through a self-service registration (see
 * LmsApprovedAccount).
 */
public interface LmsApprovedAccountService {

    /**
     * The account an LMS address belongs to, approved or not.
     */
    Optional<LmsApprovedAccount> findByHost(LmsConnector lmsConnector, String host);

    /**
     * The account an LMS address belongs to, or a new unapproved one named after it.
     */
    LmsApprovedAccount findOrCreate(LmsConnector lmsConnector, String host);

    /**
     * Whether a launch through this platform deployment may go ahead: always for one set up by
     * hand, otherwise only while its account is approved. Records the LMS's ID for the account
     * (the launch's tool_platform guid) the first time it's seen.
     */
    boolean allowsLaunch(PlatformDeployment platformDeployment, Map<String, Object> toolPlatform);

    List<LmsApprovedAccountDto> getAll();

    /**
     * @throws IllegalArgumentException when a value is missing, or a host belongs to another account
     */
    LmsApprovedAccountDto create(LmsApprovedAccountDto lmsApprovedAccountDto, String adminName);

    /**
     * @throws IllegalArgumentException when a value is missing, or a host belongs to another account
     */
    Optional<LmsApprovedAccountDto> update(UUID uuid, LmsApprovedAccountDto lmsApprovedAccountDto, String adminName);

    /**
     * A host name, lower case, from a host or a URL; null when there isn't one.
     */
    String normalizeHost(String hostOrUrl);

}
