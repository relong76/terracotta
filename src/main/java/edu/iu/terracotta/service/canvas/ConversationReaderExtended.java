package edu.iu.terracotta.service.canvas;

import java.io.IOException;
import java.util.List;
import edu.ksu.canvas.interfaces.CanvasReader;
import edu.ksu.canvas.model.Conversation;

public interface ConversationReaderExtended extends CanvasReader<Conversation, ConversationReaderExtended> {

    List<Conversation> getConversations() throws IOException;

}
