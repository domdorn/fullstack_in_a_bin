package com.dominikdorn.jsug.livechat.domain;

import java.time.Instant;

public record ChatMessage(
    Long id,
    String username,
    String content,
    Instant createdAt
) {
    public static ChatMessage create(String username, String content) {
        return new ChatMessage(null, username, content, Instant.now());
    }
}
