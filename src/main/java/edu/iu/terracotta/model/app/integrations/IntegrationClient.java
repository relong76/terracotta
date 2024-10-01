package edu.iu.terracotta.model.app.integrations;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
@Table(name = "terr_integrations_client")
public class IntegrationClient extends BaseIntegrationEntity {

    @OneToMany(mappedBy = "client")
    private List<IntegrationConfiguration> configuration;

    @Column private String name;
    @Column private String tokenVariable;
    @Column private boolean customTokenVariableAllowed;
    @Column private boolean enabled;

}
