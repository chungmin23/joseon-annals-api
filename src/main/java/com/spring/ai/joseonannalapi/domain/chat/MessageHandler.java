package com.spring.ai.joseonannalapi.domain.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ai.joseonannalapi.storage.dynamo.MessageDocument;
import com.spring.ai.joseonannalapi.storage.dynamo.MessageDynamoClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MessageHandler {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").withZone(ZoneOffset.UTC);

    private final MessageDynamoClient messageDynamoClient;
    private final ObjectMapper objectMapper;

    public MessageHandler(MessageDynamoClient messageDynamoClient, ObjectMapper objectMapper) {
        this.messageDynamoClient = messageDynamoClient;
        this.objectMapper = objectMapper;
    }

    public ChatMessage saveUserMessage(Long roomId, Long userId, String content) {
        String messageId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String createdAt = FORMATTER.format(now);

        MessageDocument doc = new MessageDocument();
        doc.setChatRoomId(roomId);
        doc.setCreatedAt(createdAt);
        doc.setMessageId(messageId);
        doc.setUserId(userId);
        doc.setRole("user");
        doc.setContent(content);
        doc.setSources(null);

        messageDynamoClient.save(doc);
        return new ChatMessage(messageId, "user", content, Collections.emptyList(), now.toEpochMilli(), null);
    }

    public ChatMessage saveAssistantMessage(Long roomId, Long personaId, String content,
                                            List<ChatSource> sources, List<String> keywords) {
        String messageId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String createdAt = FORMATTER.format(now);

        List<Map<String, Object>> sourceMaps = sources.stream()
                .map(s -> objectMapper.convertValue(s, new TypeReference<Map<String, Object>>() {}))
                .toList();

        MessageDocument doc = new MessageDocument();
        doc.setChatRoomId(roomId);
        doc.setCreatedAt(createdAt);
        doc.setMessageId(messageId);
        doc.setPersonaId(personaId);
        doc.setRole("assistant");
        doc.setContent(content);
        doc.setSources(sourceMaps);
        doc.setKeywords(keywords);

        messageDynamoClient.save(doc);
        return new ChatMessage(messageId, "assistant", content, sources, now.toEpochMilli(), keywords);
    }

    public List<ChatMessage> getRecentHistory(Long roomId, int limit) {
        return messageDynamoClient.findByRoomId(roomId, limit).stream()
                .map(doc -> {
                    List<ChatSource> sources = doc.getSources() == null
                            ? Collections.emptyList()
                            : doc.getSources().stream()
                                .map(m -> objectMapper.convertValue(m, ChatSource.class))
                                .toList();
                    long timestamp = Instant.from(FORMATTER.parse(doc.getCreatedAt())).toEpochMilli();
                    return new ChatMessage(
                            doc.getMessageId(),
                            doc.getRole(),
                            doc.getContent(),
                            sources,
                            timestamp,
                            doc.getKeywords()
                    );
                })
                .toList();
    }
}
