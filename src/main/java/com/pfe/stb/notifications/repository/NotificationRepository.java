package com.pfe.stb.notifications.repository;

import com.pfe.stb.notifications.model.Notification;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository
    extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

  List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
  void markAllAsReadByUserId(@Param("userId") UUID userId);

  @Transactional
  @Query(
      value =
          "SELECT * FROM notifications n WHERE n.type = :type AND n.data->>:key = :value AND n.user_id = :userId",
      nativeQuery = true)
  Optional<Notification> findByTypeAndDataAndUserId(
      @Param("type") String type,
      @Param("key") String key,
      @Param("value") String value,
      @Param("userId") UUID userId);

  @Modifying
  @Query("DELETE FROM Notification n WHERE n.userId = :userId")
  void deleteAllByUserId(@Param("userId") UUID userId);

  Long countByUserIdAndIsReadFalse(UUID id);
}
