package edu.iu.terracotta.connectors.generic.dao.entity.lti;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.iu.terracotta.connectors.generic.dao.entity.BaseUuidEntity;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsApprovedAccountStatus;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A school's LMS account (for Canvas, its root account) that may use Terracotta through a
 * self-service registration, such as Canvas's "1.3 Dynamic Registration". A registration from an
 * address in an APPROVED account's hosts launches right away; one from an address no account has
 * adds a PENDING account, whose launches are refused until Terracotta staff approve it. A REJECTED
 * or REVOKED account can't register again. Platform deployments set up by hand have no account
 * and aren't affected.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "lms_approved_account")
public class LmsApprovedAccount extends BaseUuidEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LmsConnector lmsConnector;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LmsApprovedAccountStatus status = LmsApprovedAccountStatus.PENDING;

    // the LMS's own ID for the account, which doesn't change with its address: for Canvas, the
    // tool_platform guid of the first launch after registering
    private String platformGuid;

    // who last changed the status, and when
    private String statusUpdatedBy;
    private Timestamp statusUpdatedAt;

    // every address the account's LMS is reached at: e.g. iu.instructure.com and canvas.iu.edu
    @Builder.Default
    @OneToMany(
        mappedBy = "lmsApprovedAccount",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    private List<LmsApprovedAccountHost> hosts = new ArrayList<>();

    // only an approved account's platform deployments launch
    @JsonIgnore
    public boolean isApproved() {
        return status == LmsApprovedAccountStatus.APPROVED;
    }

}
