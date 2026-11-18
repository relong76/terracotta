package edu.iu.terracotta.connectors.generic.dao.repository.lti;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccountHost;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;

public interface LmsApprovedAccountHostRepository extends JpaRepository<LmsApprovedAccountHost, Long> {

    Optional<LmsApprovedAccountHost> findByHostAndLmsApprovedAccount_LmsConnector(String host, LmsConnector lmsConnector);
    Optional<LmsApprovedAccountHost> findByHost(String host);

}
