package com.spring.ai.joseonannalapi.storage.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageDynamoClient {

    private final DynamoDBMapper dynamoDBMapper;

    public MessageDynamoClient(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void save(MessageDocument doc) {
        doc.setTtl(Instant.now().plus(90, ChronoUnit.DAYS).getEpochSecond());
        dynamoDBMapper.save(doc);
    }

    public List<MessageDocument> findByRoomId(Long chatRoomId, int limit) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":chatRoomId", new AttributeValue().withN(String.valueOf(chatRoomId)));

        DynamoDBQueryExpression<MessageDocument> queryExpression = new DynamoDBQueryExpression<MessageDocument>()
                .withKeyConditionExpression("chat_room_id = :chatRoomId")
                .withExpressionAttributeValues(eav)
                .withScanIndexForward(false)
                .withLimit(limit);

        return dynamoDBMapper.query(MessageDocument.class, queryExpression);
    }
}
