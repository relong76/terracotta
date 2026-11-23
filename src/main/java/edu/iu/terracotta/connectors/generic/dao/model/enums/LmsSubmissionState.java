package edu.iu.terracotta.connectors.generic.dao.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An LMS submission's state (LmsSubmission.getState), in the values connectors map their own
 * states to. Canvas's workflow_state values are used as they are.
 */
@Getter
@AllArgsConstructor
public enum LmsSubmissionState {

    DELETED("deleted"),
    GRADED("graded"),
    PENDING_REVIEW("pending_review"),
    SUBMITTED("submitted"),
    UNSUBMITTED("unsubmitted");

    private String state;

    public String state() {
        return state;
    }

}
