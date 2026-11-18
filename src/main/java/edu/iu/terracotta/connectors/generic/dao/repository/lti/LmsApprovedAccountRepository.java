package edu.iu.terracotta.connectors.generic.dao.repository.lti;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;

public interface LmsApprovedAccountRepository extends JpaRepository<LmsApprovedAccount, Long> {

    Optional<LmsApprovedAccount> findByUuid(UUID uuid);
    Optional<LmsApprovedAccount> findByLmsConnectorAndPlatformGuid(LmsConnector lmsConnector, String platformGuid);
    List<LmsApprovedAccount> findAllByOrderByNameAsc();

}
