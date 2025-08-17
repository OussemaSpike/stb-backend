package com.pfe.stb.exception;

import java.io.Serial;

public class BadRequestException extends ApplicationException {
  @Serial private static final long serialVersionUID = 1152907649742554198L;

  public BadRequestException(BadRequestExceptionType type) {
    super(type);
  }

  public BadRequestException(BadRequestExceptionType type, Throwable cause) {
    super(type, cause);
  }

  public BadRequestException(BadRequestExceptionType type, String message, Throwable cause) {
    super(type, message, cause);
  }

  public BadRequestException(
      BadRequestExceptionType type, String message, Throwable cause, Object... keyParams) {
    super(type, message, cause, keyParams);
  }

  public BadRequestException(
      BadRequestExceptionType type, Object[] valueParams, Object... keyParams) {
    super(type, valueParams, keyParams);
  }

  public BadRequestException(
      BadRequestExceptionType type, Throwable cause, Object[] valueParams, Object... keyParams) {
    super(type, cause, valueParams, keyParams);
  }

  public BadRequestException(BadRequestExceptionType type, Object... valueParams) {
    super(type, valueParams);
  }

  public enum BadRequestExceptionType implements ExceptionType {
    INVALID_REQUEST(
        "error.server.bad-request.invalid-request.title",
        "error.server.bad-request.invalid-request.msg",
        "Invalid request"),
    TOKEN_USED(
        "error.server.bad-request.token-used.title",
        "error.server.bad-request.token-used.msg",
        "Token is already used"),
    INVALID_TOKEN(
        "error.server.bad-request.invalid-token.title",
        "error.server.bad-request.invalid-token.msg",
        "Invalid token"),
    NO_PASSWORD(
        "error.server.bad-request.no-password.title",
        "error.server.bad-request.no-password.msg",
        "No password provided"),
    PASSWORDS_DO_NOT_MATCH(
        "error.server.bad-request.passwords-do-not-match.title",
        "error.server.bad-request.passwords-do-not-match.msg",
        "Passwords do not match"),
    WRONG_PASSWORD(
        "error.server.bad-request.wrong-password.title",
        "error.server.bad-request.wrong-password.msg",
        "Wrong password"),
    BENEFICIARY_ALREADY_EXISTS(
        "error.server.bad-request.beneficiary-exists.title",
        "error.server.bad-request.beneficiary-exists.msg",
        "Beneficiary already exists with this RIB"),
    NO_BANK_ACCOUNT(
        "error.server.bad-request.no-bank-account.title",
        "error.server.bad-request.no-bank-account.msg",
        "No bank account found for user"),
    TRANSFER_LIMIT_EXCEEDED(
        "error.server.bad-request.transfer-limit-exceeded.title",
        "error.server.bad-request.transfer-limit-exceeded.msg",
        "Transfer limit exceeded"),
    DAILY_LIMIT_EXCEEDED(
        "error.server.bad-request.daily-limit-exceeded.title",
        "error.server.bad-request.daily-limit-exceeded.msg",
        "Daily transfer limit exceeded"),
    MONTHLY_LIMIT_EXCEEDED(
        "error.server.bad-request.monthly-limit-exceeded.title",
        "error.server.bad-request.monthly-limit-exceeded.msg",
        "Monthly transfer limit exceeded"),
    INVALID_VALIDATION_CODE(
        "error.server.bad-request.invalid-validation-code.title",
        "error.server.bad-request.invalid-validation-code.msg",
        "Invalid validation code"),
    INVALID_TRANSFER_STATUS(
        "error.server.bad-request.invalid-transfer-status.title",
        "error.server.bad-request.invalid-transfer-status.msg",
        "Invalid transfer status"),
    INSUFFICIENT_BALANCE(
        "error.server.bad-request.insufficient-balance.title",
        "error.server.bad-request.insufficient-balance.msg",
        "Insufficient balance for transfer"),
    INVALID_USER_ROLE_FOR_OPERATION(
        "error.server.bad-request.invalid-user-role-for-operation.title",
        "error.server.bad-request.invalid-user-role-for-operation.msg",
        "Invalid user role for this operation"),
    USER_ALREADY_ENABLED(
        "error.server.bad-request.user-already-enabled.title",
        "error.server.bad-request.user-already-enabled.msg",
        "User is already enabled"),
    USER_ALREADY_DISABLED(
        "error.server.bad-request.user-already-disabled.title",
        "error.server.bad-request.user-already-disabled.msg",
        "User is already disabled");

    private final String messageKey;
    private final String titleKey;
    private final String messageCause;

    BadRequestExceptionType(String titleKey, String messageKey, String messageCause) {
      this.messageKey = messageKey;
      this.titleKey = titleKey;
      this.messageCause = messageCause;
    }

    @Override
    public String getTitleKey() {
      return titleKey;
    }

    @Override
    public String getMessageKey() {
      return messageKey;
    }

    @Override
    public String getMessageCause() {
      return messageCause;
    }
  }
}
