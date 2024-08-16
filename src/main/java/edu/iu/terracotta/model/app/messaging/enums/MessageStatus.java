package edu.iu.terracotta.model.app.messaging.enums;

import java.util.List;

public enum MessageStatus {

    CANCELLED,
    COPIED,
    CREATED,
    DELETED,
    EDITED,
    ERROR,
    PROCESSING,
    QUEUED,
    READY,
    SENT;

    public static List<MessageStatus> displayable() {
        return List.of(
            COPIED,
            CREATED,
            EDITED,
            PROCESSING,
            QUEUED,
            READY,
            SENT
        );
    }

}
