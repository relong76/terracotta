package edu.iu.terracotta.model.app.integrations;

import edu.iu.terracotta.model.app.integrations.enums.IntegrationTokenStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "terr_integrations_token_log")
public class IntegrationTokenLog extends BaseIntegrationEntity {

    @ManyToOne
    @JoinColumn(name = "token_id")
    private IntegrationToken integrationToken;

    @Column
    @Enumerated(EnumType.STRING)
    private IntegrationTokenStatus status;

    @Column private String score;
    @Column private String error;
    @Column String token;

}
