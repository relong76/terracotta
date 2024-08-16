package edu.iu.terracotta.repository.messaging;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iu.terracotta.model.app.messaging.MessageRule;

@SuppressWarnings({"PMD.MethodNamingConventions"})
public interface MessageRuleRepository extends JpaRepository<MessageRule, Long> {

    Optional<MessageRule> findByUuid(UUID uuid);
    List<MessageRule> findAllByContent_Id(long contentId);

}
