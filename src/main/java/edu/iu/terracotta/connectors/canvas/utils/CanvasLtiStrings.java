package edu.iu.terracotta.connectors.canvas.utils;

import lombok.experimental.UtilityClass;

/**
 * Canvas's own LTI extensions: claims, scopes and values only Canvas uses. The LTI standard's
 * values are in LtiStrings.
 */
@UtilityClass
public final class CanvasLtiStrings {

    // Canvas's extension on an AGS score (e.g. new_submission, submitted_at)
    public static final String SUBMISSION = "https://canvas.instructure.com/lti/submission";

}
