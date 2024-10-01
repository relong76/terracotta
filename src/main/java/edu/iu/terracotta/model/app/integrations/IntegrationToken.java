package edu.iu.terracotta.model.app.integrations;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.Submission;
import edu.iu.terracotta.model.app.integrations.enums.IntegrationTokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "terr_integrations_token")
public class IntegrationToken extends BaseIntegrationEntity {

    @ManyToOne
    @JoinColumn(
        name = "lti_user_user_id",
        nullable = false
    )
    private LtiUserEntity user;

    @ManyToOne
    @JoinColumn(
        name = "integration_id",
        nullable = false
    )
    private Integration integration;

    @Column
    @Enumerated(EnumType.STRING)
    private IntegrationTokenType type;

    @ManyToOne
    @JoinColumn(
        name = "submission_id",
        nullable = false
    )
    private Submission submission;

    @OneToMany(mappedBy = "integrationToken")
    private List<IntegrationTokenLog> logs;

    @Column private String token;
    @Column private Timestamp expiresAt;
    @Column private Timestamp redeemedAt;

    /**
     * Token is valid if:
     *
     * 1. it has not been redeemed
     * and
     * 2. the expiration is after the current time
     *
     * @return
     */
    @Transient
    public boolean isValid() {
        return redeemedAt == null && Timestamp.from(Instant.now()).before(expiresAt);
    }

}
