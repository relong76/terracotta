package edu.iu.terracotta.connectors.generic.dao.model.enums;

/**
 * Where an LmsApprovedAccount stands. Only an APPROVED account's platform deployments launch.
 */
public enum LmsApprovedAccountStatus {

    // registered from an address no account had, waiting for Terracotta staff
    PENDING,
    APPROVED,
    // turned down while pending: can't register again
    REJECTED,
    // approved once, then withdrawn: can't register again, and its launches stop
    REVOKED

}
