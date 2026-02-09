package com.dominikdorn.jsug.livechat.adapter.ui;

import com.dominikdorn.jsug.livechat.domain.ChatMessage;
import com.dominikdorn.jsug.livechat.domain.ChatMessageRepository;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Route("")
public class ChatView extends VerticalLayout {

    private final ChatMessageRepository repository;
    private final Div messageList;
    private final TextField usernameField;
    private final TextField messageField;
    private Registration broadcasterRegistration;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    public ChatView(ChatMessageRepository repository) {
        this.repository = repository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Title
        Div title = new Div();
        title.setText("Live Chat");
        title.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("margin-bottom", "10px");
        add(title);

        // Message list
        messageList = new Div();
        messageList.setWidthFull();
        messageList.getStyle()
                .set("flex-grow", "1")
                .set("overflow-y", "auto")
                .set("border", "1px solid #ccc")
                .set("border-radius", "4px")
                .set("padding", "10px")
                .set("background-color", "#f9f9f9");
        add(messageList);
        setFlexGrow(1, messageList);

        // Input area
        usernameField = new TextField();
        usernameField.setPlaceholder("Username");
        usernameField.setWidth("150px");

        messageField = new TextField();
        messageField.setPlaceholder("Type a message...");
        messageField.setWidthFull();

        Button sendButton = new Button("Send");
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.addClickListener(e -> sendMessage());
        sendButton.addClickShortcut(Key.ENTER);

        HorizontalLayout inputLayout = new HorizontalLayout(usernameField, messageField, sendButton);
        inputLayout.setWidthFull();
        inputLayout.setFlexGrow(1, messageField);
        add(inputLayout);

        // Load recent messages
        loadRecentMessages();
    }

    private void loadRecentMessages() {
        repository.findRecentMessages(50).forEach(this::displayMessage);
        scrollToBottom();
    }

    private void sendMessage() {
        String username = usernameField.getValue().trim();
        String content = messageField.getValue().trim();

        if (username.isEmpty()) {
            usernameField.focus();
            return;
        }

        if (content.isEmpty()) {
            messageField.focus();
            return;
        }

        ChatMessage message = ChatMessage.create(username, content);
        ChatMessage saved = repository.save(message);

        Broadcaster.broadcast(saved);
        messageField.clear();
        messageField.focus();
    }

    private void displayMessage(ChatMessage message) {
        Div messageDiv = new Div();
        messageDiv.getStyle()
                .set("padding", "8px")
                .set("margin-bottom", "8px")
                .set("background-color", "#fff")
                .set("border-radius", "4px")
                .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)");

        Span timeSpan = new Span(TIME_FORMATTER.format(message.createdAt()));
        timeSpan.getStyle()
                .set("color", "#888")
                .set("font-size", "12px")
                .set("margin-right", "8px");

        Span usernameSpan = new Span(message.username() + ":");
        usernameSpan.getStyle()
                .set("font-weight", "bold")
                .set("color", "#333")
                .set("margin-right", "8px");

        Span contentSpan = new Span(message.content());
        contentSpan.getStyle().set("color", "#555");

        messageDiv.add(timeSpan, usernameSpan, contentSpan);
        messageList.add(messageDiv);
    }

    private void scrollToBottom() {
        messageList.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Broadcaster.register(message -> {
            ui.access(() -> {
                displayMessage(message);
                scrollToBottom();
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }
}
