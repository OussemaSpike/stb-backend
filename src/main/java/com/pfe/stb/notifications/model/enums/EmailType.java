package com.pfe.stb.notifications.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum EmailType {
  ACCOUNT_CONFIRMATION("Account Confirmation", "emails/confirmationAccount", "/activate-account"),
  RESET_PASSWORD("Password Reset", "emails/resetPassword", "/reset-password"),
  SET_PASSWORD("Set Password", "emails/setPassword", "/set-password"),
  
  // Transfer related email templates
  NEW_TRANSFER_NOTIFICATION("Nouvelle demande de virement", "emails/newTransferNotification", "/admin/transfers"),
  TRANSFER_APPROVED("Virement approuvé", "emails/transferApproved", "/transfers"),
  TRANSFER_REJECTED("Virement rejeté", "emails/transferRejected", "/transfers"),
  TRANSFER_COMPLETED("Virement terminé", "emails/transferCompleted", "/transfers"),
  TRANSFER_FAILED("Virement échoué", "emails/transferFailed", "/transfers");

  @Setter private static String frontendUrl;

  private final String subject;
  private final String templateName;
  private final String frontendPath;

  public String getFullPath() {
    return frontendUrl + frontendPath;
  }
}
