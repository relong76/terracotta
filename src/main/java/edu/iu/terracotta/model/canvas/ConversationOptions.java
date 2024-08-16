package edu.iu.terracotta.model.canvas;

import java.util.List;

import edu.ksu.canvas.requestOptions.BaseOptions;

public class ConversationOptions extends BaseOptions {

    public ConversationOptions recipients(List<String> recipients) {
        if (recipients == null) {
            return this;
        }

        addStringList("recipients[]", recipients);
        return this;
    }

    public ConversationOptions attachments(List<String> attachments) {
        if (attachments == null) {
            return this;
        }

        addStringList("attachment_ids[]", attachments);
        return this;
    }

    public ConversationOptions forceNew(boolean forceNew) {
        addSingleItem("force_new", Boolean.toString(forceNew));
        return this;
    }

    public ConversationOptions contextCode(String contextCode) {
        if (contextCode == null) {
            return this;
        }

        addSingleItem("context_code", String.format("course_%s", contextCode));
        return this;
    }

    public ConversationOptions subject(String subject) {
        if (subject == null) {
            return this;
        }

        addSingleItem("subject", subject);
        return this;
    }

    public ConversationOptions body(String body) {
        if (body == null) {
            return this;
        }

        addSingleItem("body", body);
        return this;
    }

}
