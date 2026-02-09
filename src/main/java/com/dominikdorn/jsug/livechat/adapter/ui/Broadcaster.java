package com.dominikdorn.jsug.livechat.adapter.ui;

import com.dominikdorn.jsug.livechat.domain.ChatMessage;
import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Broadcaster {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final LinkedList<Consumer<ChatMessage>> listeners = new LinkedList<>();

    public static synchronized Registration register(Consumer<ChatMessage> listener) {
        listeners.add(listener);
        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(ChatMessage message) {
        for (Consumer<ChatMessage> listener : listeners) {
            executor.execute(() -> listener.accept(message));
        }
    }
}
