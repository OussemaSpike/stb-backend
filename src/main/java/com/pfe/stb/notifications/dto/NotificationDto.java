package com.pfe.stb.notifications.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pfe.stb.notifications.model.enums.NotificationType;
import java.time.Instant;
import java.util.Map;

@JsonInclude(NON_NULL)
public record NotificationDto(
    String id,
    String userId,
    Boolean isRead,
    Map<String, Object> data,
    NotificationType type,
    Instant createdAt,
    Instant updatedAt) {}
