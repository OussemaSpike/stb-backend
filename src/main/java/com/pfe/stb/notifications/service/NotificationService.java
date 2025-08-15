package com.pfe.stb.notifications.service;

import com.pfe.stb.exception.NotFoundException;
import com.pfe.stb.notifications.model.Notification;
import com.pfe.stb.notifications.model.enums.NotificationType;
import com.pfe.stb.notifications.port.NotificationUseCases;
import com.pfe.stb.notifications.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCases {

  private final NotificationRepository notificationRepository;

  // Create a new notification
  @Override
  public Notification createNotification(Notification notification) {
    return notificationRepository.save(notification);
  }

  // Create a list of notifications
  @Override
  public void createBulk(List<Notification> notifications) {
    notificationRepository.saveAll(notifications);
  }

  // Get all notifications by user id
  @Override
  public Page<Notification> getAllByUserId(UUID userId, Pageable pageable) {
    Specification<Notification> spec = NotificationSpecification.hasUserId(userId);
    return notificationRepository.findAll(spec, pageable);
  }

  // Get all notifications by user id without pagination
  @Override
  public List<Notification> getAllByUserIdWithoutPagination(UUID userId) {
    return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
  }

  // mark a notification as read
  @Override
  public Notification markAsRead(UUID id) {
    Notification notificationsEntity =
        this.notificationRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.NOTIFICATION_NOT_FOUND,
                        id.toString()));
    notificationsEntity.setIsRead(true);
    return this.notificationRepository.save(notificationsEntity);
  }

  // mark all notifications as read
  @Override
  public boolean markAllAsRead(UUID userId) {
    try {
      notificationRepository.markAllAsReadByUserId(userId);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // delete a notification by id
  @Override
  public void deleteById(UUID id) {
    this.notificationRepository.deleteById(id);
  }

  // delete all notifications by user id
  @Override
  public void deleteAll(UUID userId) {
    this.notificationRepository.deleteAllByUserId(userId);
  }

  @Override
  public Long getUnreadNotificationsCount(UUID id) {
    return this.notificationRepository.countByUserIdAndIsReadFalse(id);
  }

  @Override
  public Optional<Notification> findByTypeAndDataAndUserId(
      NotificationType type, String jsonKey, String jsonValue, UUID userId) {
    return this.notificationRepository.findByTypeAndDataAndUserId(
        type.name(), jsonKey, jsonValue, userId);
  }
}
