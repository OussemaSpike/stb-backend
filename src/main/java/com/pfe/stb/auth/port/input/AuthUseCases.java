package com.pfe.stb.auth.port.input;

import com.pfe.stb.auth.model.AuthResponse;
import com.pfe.stb.user.model.User;
import jakarta.mail.MessagingException;
import java.util.UUID;

/** Interface representing the use cases for authentication. */
public interface AuthUseCases {

  /**
   * Signs up a new user.
   *
   * @param authUser the user to be signed up
   * @param confirmPassword the confirmation of the user's password
   * @return the signed-up user
   * @throws MessagingException if there is an error sending the confirmation email
   */
  User signUp(User authUser, String confirmPassword) throws MessagingException;

  /**
   * Activates a user account using the provided activation code.
   *
   * @param code the activation code to activate the account
   * @throws MessagingException if there is an error sending the activation email
   */
  void activateAccount(String code) throws MessagingException;

  /**
   * Sends an activation code to the provided email address.
   *
   * @param email the email address to send the activation code to
   * @throws MessagingException if there is an error sending the activation email
   */
  void sendActivationCode(String email) throws MessagingException;

  /**
   * Sends a reset password code to the provided email address.
   *
   * @param email the email address to send the reset password code to
   * @throws MessagingException if there is an error sending the reset password email
   */
  void forgetPassword(String email) throws MessagingException;

  /**
   * Resets the password for the user associated with the provided code.
   *
   * @param code the reset password code
   * @param password the new password
   * @param confirmPassword the confirmation of the new password
   * @throws MessagingException if there is an error sending the reset password email
   */
  void resetPassword(String code, String password, String confirmPassword)
      throws MessagingException;

  /**
   * Authenticates a user with the provided email and password.
   *
   * @param email the email address of the user
   * @param password the password of the user
   * @return an AccessToken object containing the authentication token and related information
   */
  AuthResponse signIn(String email, String password);

  /**
   * Refreshes the authentication token using the provided refresh token.
   *
   * @param refreshToken the refresh token
   * @return an AccessToken object containing the new authentication token and related information
   */
  AuthResponse refreshToken(String refreshToken);

  /**
   * Changes the password for the user with the provided user ID.
   *
   * @param id the ID of the user
   * @param oldPassword the old password
   * @param newPassword the new password
   * @param confirmPassword the confirmation of the new password
   */
  void changePassword(UUID id, String oldPassword, String newPassword, String confirmPassword);
}
