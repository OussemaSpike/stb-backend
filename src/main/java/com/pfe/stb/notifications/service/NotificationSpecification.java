package com.pfe.stb.notifications.service;

import com.pfe.stb.notifications.model.Notification;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecification {

  private NotificationSpecification() {}

  public static Specification<Notification> hasUserId(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("userId"), userId);
  }
}
