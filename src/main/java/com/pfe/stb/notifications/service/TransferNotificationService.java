package com.pfe.stb.notifications.service;

import com.pfe.stb.notifications.model.Notification;
import com.pfe.stb.notifications.model.enums.EmailType;
import com.pfe.stb.notifications.model.enums.NotificationType;
import com.pfe.stb.notifications.port.Emails;
import com.pfe.stb.notifications.port.NotificationUseCases;
import com.pfe.stb.transfer.model.Transfer;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import com.pfe.stb.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransferNotificationService {

  private static final DateTimeFormatter FRENCH_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
  private final NotificationUseCases notificationUseCases;
  private final Emails emailService;
  private final UserRepository userRepository;

  /** Notify admins about a new transfer request */
  @Async
  public void notifyNewTransfer(Transfer transfer) {
    try {
      // Get all admin users
      List<User> admins = userRepository.findByRoles_Name(RoleType.ADMIN);

      // Create notification data
      Map<String, Object> notificationData = createTransferNotificationData(transfer);

      for (User admin : admins) {
        // Create notification
        Notification notification =
            Notification.builder()
                .type(NotificationType.NEW_TRANSFER_CREATED)
                .userId(admin.getId())
                .data(notificationData)
                .isRead(false)
                .build();

        notificationUseCases.createNotification(notification);
        log.info(
            "Created notification for admin {} about new transfer {}",
            admin.getId(),
            transfer.getReference());

        // Send email
        sendNewTransferEmailToAdmin(admin, transfer);
      }
    } catch (Exception e) {
      log.error("Failed to notify admins about new transfer {}", transfer.getReference(), e);
    }
  }

  /** Notify sender and receiver about transfer approval */
  @Async
  public void notifyTransferApproved(Transfer transfer) {
    try {
      Map<String, Object> notificationData = createTransferNotificationData(transfer);

      // Notify sender
      Notification senderNotification =
          Notification.builder()
              .type(NotificationType.TRANSFER_APPROVED)
              .userId(transfer.getUser().getId())
              .data(notificationData)
              .isRead(false)
              .build();

      notificationUseCases.createNotification(senderNotification);
      sendTransferApprovedEmail(transfer.getUser(), transfer);
      log.info(
          "Notified sender {} about approved transfer {}",
          transfer.getUser().getId(),
          transfer.getReference());

      // Notify receiver if they are a user in our system
      notifyReceiverIfExists(transfer);

    } catch (Exception e) {
      log.error("Failed to notify about approved transfer {}", transfer.getReference(), e);
    }
  }

  /** Notify sender about transfer rejection */
  @Async
  public void notifyTransferRejected(Transfer transfer) {
    try {
      Map<String, Object> notificationData = createTransferNotificationData(transfer);
      notificationData.put("rejectionReason", transfer.getRejectionReason());

      // Notify sender
      Notification senderNotification =
          Notification.builder()
              .type(NotificationType.TRANSFER_REJECTED)
              .userId(transfer.getUser().getId())
              .data(notificationData)
              .isRead(false)
              .build();

      notificationUseCases.createNotification(senderNotification);
      sendTransferRejectedEmail(transfer.getUser(), transfer);
      log.info(
          "Notified sender {} about rejected transfer {}",
          transfer.getUser().getId(),
          transfer.getReference());

    } catch (Exception e) {
      log.error("Failed to notify about rejected transfer {}", transfer.getReference(), e);
    }
  }

  /** Notify about transfer failure */
  @Async
  public void notifyTransferFailed(Transfer transfer) {
    try {
      Map<String, Object> notificationData = createTransferNotificationData(transfer);
      notificationData.put("failureReason", transfer.getFailureReason());

      // Notify sender
      Notification senderNotification =
          Notification.builder()
              .type(NotificationType.TRANSFER_FAILED)
              .userId(transfer.getUser().getId())
              .data(notificationData)
              .isRead(false)
              .build();

      notificationUseCases.createNotification(senderNotification);
      sendTransferFailedEmail(transfer.getUser(), transfer);
      log.info(
          "Notified sender {} about failed transfer {}",
          transfer.getUser().getId(),
          transfer.getReference());

    } catch (Exception e) {
      log.error("Failed to notify about failed transfer {}", transfer.getReference(), e);
    }
  }

  /** Notify receiver if they exist in our system */
  private void notifyReceiverIfExists(Transfer transfer) {
    try {
      // Find receiver by RIB
      userRepository
          .findByBankAccount_Rib(transfer.getBeneficiary().getRib())
          .ifPresent(
              receiver -> {
                Map<String, Object> notificationData = createTransferNotificationData(transfer);
                notificationData.put("message", "Vous avez reçu un virement");
                notificationData.put("isReceiver", true);

                Notification receiverNotification =
                    Notification.builder()
                        .type(NotificationType.TRANSFER_COMPLETED)
                        .userId(receiver.getId())
                        .data(notificationData)
                        .isRead(false)
                        .build();

                notificationUseCases.createNotification(receiverNotification);

                // Send email to receiver
                try {
                  Map<String, Object> emailData = createEmailData(transfer, receiver);
                  emailData.put("message", "Vous avez reçu un virement");
                  emailData.put("isReceiver", true);
                  emailService.sendEmail(
                      receiver.getEmail(), EmailType.TRANSFER_COMPLETED, emailData);
                } catch (MessagingException e) {
                  log.error("Failed to send email to receiver {}", receiver.getEmail(), e);
                }

                log.info(
                    "Notified receiver {} about transfer {}",
                    receiver.getId(),
                    transfer.getReference());
              });
    } catch (Exception e) {
      log.error("Failed to notify receiver about transfer {}", transfer.getReference(), e);
    }
  }

  /** Create notification data map */
  private Map<String, Object> createTransferNotificationData(Transfer transfer) {
    Map<String, Object> data = new HashMap<>();
    data.put("transferId", transfer.getId().toString());
    data.put("reference", transfer.getReference());
    data.put("amount", transfer.getAmount().toString());
    data.put("currency", transfer.getCurrency());
    data.put("beneficiaryName", transfer.getBeneficiary().getName());
    data.put("beneficiaryRib", transfer.getBeneficiary().getRib());
    data.put("reason", transfer.getReason());
    data.put("status", transfer.getStatus().name());
    data.put("senderName", transfer.getUser().getFullName());
    data.put("createdAt", transfer.getCreatedAt());
    return data;
  }

  /** Create email template data */
  private Map<String, Object> createEmailData(Transfer transfer, User recipient) {
    Map<String, Object> data = new HashMap<>();
    data.put("recipientName", recipient.getFullName());
    data.put("transferReference", transfer.getReference());
    data.put("amount", transfer.getAmount().toString());
    data.put("currency", transfer.getCurrency());
    data.put("beneficiaryName", transfer.getBeneficiary().getName());
    data.put("beneficiaryRib", transfer.getBeneficiary().getRib());
    data.put("reason", transfer.getReason());
    data.put("senderName", transfer.getUser().getFullName());
    data.put("createdAt", transfer.getCreatedAt());

    if (transfer.getApprovedAt() != null) {
      data.put("approvedAt", transfer.getApprovedAt().format(FRENCH_DATE_FORMATTER));
    }
    if (transfer.getRejectedAt() != null) {
      data.put("rejectedAt", transfer.getRejectedAt().format(FRENCH_DATE_FORMATTER));
    }
    if (transfer.getRejectionReason() != null) {
      data.put("rejectionReason", transfer.getRejectionReason());
    }
    if (transfer.getFailureReason() != null) {
      data.put("failureReason", transfer.getFailureReason());
    }

    return data;
  }

  /** Send email to admin about new transfer */
  private void sendNewTransferEmailToAdmin(User admin, Transfer transfer) {
    try {
      Map<String, Object> emailData = createEmailData(transfer, admin);
      emailData.put("isAdmin", true);
      emailService.sendEmail(admin.getEmail(), EmailType.NEW_TRANSFER_NOTIFICATION, emailData);
      log.info("Sent new transfer email to admin {}", admin.getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send new transfer email to admin {}", admin.getEmail(), e);
    }
  }

  /** Send transfer approved email */
  private void sendTransferApprovedEmail(User user, Transfer transfer) {
    try {
      Map<String, Object> emailData = createEmailData(transfer, user);
      emailService.sendEmail(user.getEmail(), EmailType.TRANSFER_APPROVED, emailData);
      log.info("Sent transfer approved email to {}", user.getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send transfer approved email to {}", user.getEmail(), e);
    }
  }

  /** Send transfer rejected email */
  private void sendTransferRejectedEmail(User user, Transfer transfer) {
    try {
      Map<String, Object> emailData = createEmailData(transfer, user);
      emailService.sendEmail(user.getEmail(), EmailType.TRANSFER_REJECTED, emailData);
      log.info("Sent transfer rejected email to {}", user.getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send transfer rejected email to {}", user.getEmail(), e);
    }
  }

  /** Send transfer failed email */
  private void sendTransferFailedEmail(User user, Transfer transfer) {
    try {
      Map<String, Object> emailData = createEmailData(transfer, user);
      emailService.sendEmail(user.getEmail(), EmailType.TRANSFER_FAILED, emailData);
      log.info("Sent transfer failed email to {}", user.getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send transfer failed email to {}", user.getEmail(), e);
    }
  }
}
