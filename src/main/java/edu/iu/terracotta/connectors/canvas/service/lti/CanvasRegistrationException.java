package edu.iu.terracotta.connectors.canvas.service.lti;

/**
 * A Canvas Dynamic Registration that can't go ahead. The message is shown to the Canvas admin
 * registering Terracotta, so it says what to do, and holds nothing secret.
 */
public class CanvasRegistrationException extends Exception {

    private static final long serialVersionUID = 1L;

    public CanvasRegistrationException(String message) {
        super(message);
    }

    public CanvasRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
