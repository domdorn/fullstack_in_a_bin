package com.dominikdorn.jsug.livechat.domain;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findRecentMessages(int limit);
    List<ChatMessage> findMessagesAfter(Instant timestamp);
}
