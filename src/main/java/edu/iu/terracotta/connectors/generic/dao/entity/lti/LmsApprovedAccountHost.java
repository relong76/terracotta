package edu.iu.terracotta.connectors.generic.dao.entity.lti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.iu.terracotta.connectors.generic.dao.entity.BaseUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One address (host name, lower case) an LmsApprovedAccount's LMS is reached at.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "lms_approved_account_host")
public class LmsApprovedAccountHost extends BaseUuidEntity {

    @Column(nullable = false, unique = true)
    private String host;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "lms_approved_account_id",
        nullable = false
    )
    private LmsApprovedAccount lmsApprovedAccount;

}
