package com.dominikdorn.jsug.livechat.adapter.persistence;

import com.dominikdorn.jsug.livechat.domain.ChatMessage;
import com.dominikdorn.jsug.livechat.domain.ChatMessageRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class JdbcChatMessageRepository implements ChatMessageRepository {

    private final JdbcClient jdbcClient;

    public JdbcChatMessageRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql("INSERT INTO chat_message (username, content, created_at) VALUES (?, ?, ?)")
                .param(message.username())
                .param(message.content())
                .param(Timestamp.from(message.createdAt()))
                .update(keyHolder, "id");

        Long id = keyHolder.getKeyAs(Long.class);
        return new ChatMessage(id, message.username(), message.content(), message.createdAt());
    }

    @Override
    public List<ChatMessage> findRecentMessages(int limit) {
        return jdbcClient.sql("""
                SELECT id, username, content, created_at
                FROM chat_message
                ORDER BY created_at DESC
                LIMIT ?
                """)
                .param(limit)
                .query((rs, rowNum) -> new ChatMessage(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toInstant()
                ))
                .list()
                .reversed();
    }

    @Override
    public List<ChatMessage> findMessagesAfter(Instant timestamp) {
        return jdbcClient.sql("""
                SELECT id, username, content, created_at
                FROM chat_message
                WHERE created_at > ?
                ORDER BY created_at ASC
                """)
                .param(Timestamp.from(timestamp))
                .query((rs, rowNum) -> new ChatMessage(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toInstant()
                ))
                .list();
    }
}
