package edu.iu.terracotta.security.pii;

import org.springframework.stereotype.Component;

import edu.iu.terracotta.connectors.generic.dao.entity.lti.LtiUserEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;

/**
 * Keeps an LTI user's email hash in step with their (encrypted) email, so the email can still be
 * looked up and grouped by in queries.
 */
@Component
@RequiredArgsConstructor
public class LtiUserPiiListener {

    private final PiiCipher piiCipher;

    @PrePersist
    @PreUpdate
    public void updateEmailHash(LtiUserEntity ltiUserEntity) {
        ltiUserEntity.setEmailHash(piiCipher.hashEmail(ltiUserEntity.getEmail()));
    }

}
