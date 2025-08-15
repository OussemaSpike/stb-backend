package com.pfe.stb.notifications.port;

import com.pfe.stb.notifications.model.Notification;
import com.pfe.stb.notifications.model.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationUseCases {

  // Create a new notification
  Notification createNotification(Notification notification);

  /**
   * Create a list of notifications
   *
   * @param notifications
   */
  void createBulk(List<Notification> notifications);

  /**
   * Get all notifications by user id
   *
   * @param userId
   * @return
   */
  Page<Notification> getAllByUserId(UUID userId, Pageable pageable);

  /**
   * Get all notifications by user id without pagination
   *
   * @param userId
   * @return List of all notifications for the user
   */
  List<Notification> getAllByUserIdWithoutPagination(UUID userId);

  /**
   * Mark a notification as read
   *
   * @param id
   * @return
   */
  Notification markAsRead(UUID id);

  /**
   * Mark all notifications as read
   *
   * @param userId
   * @return
   */
  boolean markAllAsRead(UUID userId);

  /**
   * Delete a notification by id
   *
   * @param id
   */
  void deleteById(UUID id);

  /** Delete all notifications */
  @Transactional
  void deleteAll(UUID userId);

  Long getUnreadNotificationsCount(UUID id);

  Optional<Notification> findByTypeAndDataAndUserId(
      NotificationType type, String jsonKey, String jsonValue, UUID userId);
}
