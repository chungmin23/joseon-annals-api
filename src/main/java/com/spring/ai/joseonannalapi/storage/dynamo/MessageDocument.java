package com.spring.ai.joseonannalapi.storage.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@DynamoDBTable(tableName = "messages")
@Getter
@Setter
@NoArgsConstructor
public class MessageDocument {

    @DynamoDBHashKey(attributeName = "chat_room_id")
    private Long chatRoomId;

    @DynamoDBRangeKey(attributeName = "created_at")
    private String createdAt;

    @DynamoDBAttribute(attributeName = "messageId")
    private String messageId;

    @DynamoDBAttribute(attributeName = "userId")
    private Long userId;

    @DynamoDBAttribute(attributeName = "personaId")
    private Long personaId;

    @DynamoDBAttribute(attributeName = "role")
    private String role;

    @DynamoDBAttribute(attributeName = "content")
    private String content;

    @DynamoDBTypeConvertedJson
    @DynamoDBAttribute(attributeName = "sources_json")
    private List<Map<String, Object>> sources;

    @DynamoDBTypeConvertedJson
    @DynamoDBAttribute(attributeName = "keywords")
    private List<String> keywords;

    @DynamoDBAttribute(attributeName = "ttl")
    private Long ttl;
}
