package edu.iu.terracotta.exceptions.messaging;

public class MessageContentAttachmentException extends Exception {

    public MessageContentAttachmentException(String message) {
        super(message);
    }

    public MessageContentAttachmentException(String message, Throwable e) {
        super(message, e);
    }

}
