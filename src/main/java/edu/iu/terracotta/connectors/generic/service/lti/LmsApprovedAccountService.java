package edu.iu.terracotta.connectors.generic.service.lti;

import java.util.List;
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
     * A school's account: by its LMS account ID when there is one, otherwise by its address.
     *
     * @throws IllegalStateException when the address belongs to an account with a different LMS account ID
     */
    Optional<LmsApprovedAccount> find(LmsConnector lmsConnector, String host, String platformGuid);

    /**
     * A school's account (see find), with its LMS account ID filled in if it had none, or a new
     * PENDING one for it, named after the school (or its address).
     *
     * @throws IllegalStateException when the address belongs to an account with a different LMS account ID
     */
    LmsApprovedAccount findOrCreate(LmsConnector lmsConnector, String host, String platformGuid, String name);

    /**
     * Whether a launch through this platform deployment may go ahead: always for one set up by
     * hand, otherwise only while its account is APPROVED.
     */
    boolean allowsLaunch(PlatformDeployment platformDeployment);

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
