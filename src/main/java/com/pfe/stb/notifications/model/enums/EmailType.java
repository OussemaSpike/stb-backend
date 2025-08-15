package com.pfe.stb.notifications.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum EmailType {
  ACCOUNT_CONFIRMATION("Account Confirmation", "confirmationAccount", "/activate-account"),
  RESET_PASSWORD("Password Reset", "resetPassword", "/reset-password"),
  SET_PASSWORD("Set Password", "setPassword", "/set-password");

  @Setter private static String frontendUrl;

  private final String subject;
  private final String templateName;
  private final String frontendPath;

  public String getFullPath() {
    return frontendUrl + frontendPath;
  }
}
