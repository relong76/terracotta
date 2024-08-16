package edu.iu.terracotta.exceptions.messaging;

public class MessageProcessingException extends Exception {

    public MessageProcessingException(String message, Throwable e) {
        super(message, e);
    }

}
