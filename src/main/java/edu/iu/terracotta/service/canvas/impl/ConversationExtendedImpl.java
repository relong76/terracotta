package edu.iu.terracotta.service.canvas.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import com.google.common.reflect.TypeToken;

import edu.iu.terracotta.service.canvas.ConversationReaderExtended;
import edu.ksu.canvas.impl.BaseImpl;
import edu.ksu.canvas.interfaces.ConversationWriter;
import edu.ksu.canvas.model.Conversation;
import edu.ksu.canvas.net.RestClient;
import edu.ksu.canvas.oauth.OauthToken;

public class ConversationExtendedImpl extends BaseImpl<Conversation, ConversationReaderExtended, ConversationWriter> implements ConversationReaderExtended {

    public ConversationExtendedImpl(String canvasBaseUrl, Integer apiVersion, OauthToken oauthToken, RestClient restClient, int connectTimeout, int readTimeout, Integer paginationPageSize, Boolean serializeNulls) {
        super(canvasBaseUrl, apiVersion, oauthToken, restClient, connectTimeout, readTimeout, paginationPageSize, serializeNulls);
    }

    @Override
    public List<Conversation> getConversations() throws IOException {
        String url = buildCanvasUrl("conversations", Collections.emptyMap());

        return this.getListFromCanvas(url);
    }

    @Override
    protected Type listType() {
        return new TypeToken<List<Conversation>>() {}.getType();
    }

    @Override
    protected Class<Conversation> objectType() {
        return Conversation.class;
    }

}
