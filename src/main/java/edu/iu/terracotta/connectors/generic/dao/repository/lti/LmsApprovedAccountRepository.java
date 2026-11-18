package edu.iu.terracotta.connectors.generic.dao.repository.lti;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LmsApprovedAccount;

public interface LmsApprovedAccountRepository extends JpaRepository<LmsApprovedAccount, Long> {

    Optional<LmsApprovedAccount> findByUuid(UUID uuid);
    List<LmsApprovedAccount> findAllByOrderByNameAsc();

}
