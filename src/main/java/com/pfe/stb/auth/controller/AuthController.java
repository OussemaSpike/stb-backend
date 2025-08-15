package com.pfe.stb.auth.controller;

import static com.pfe.stb.shared.security.AuthUtils.getCurrentAuthenticatedUserId;

import com.pfe.stb.auth.dto.request.*;
import com.pfe.stb.auth.dto.response.SignUpResponseDto;
import com.pfe.stb.auth.model.AuthResponse;
import com.pfe.stb.auth.port.input.AuthUseCases;
import com.pfe.stb.user.mapper.UserMapper;
import com.pfe.stb.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(
    name = "Auth",
    description =
        "Operations related to managing authentication, including signing up, signing in, and resetting passwords.")
@RequiredArgsConstructor
public class AuthController {

  private final AuthUseCases authUseCases;
  private final UserMapper userMapper;

  @Operation(
      summary = "Sign up a new user",
      description = "Register a new user account with an optional subscription to the newsletter.")
  @PostMapping("/register")
  public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpDto signUpRequest)
      throws MessagingException {

    User authUser = userMapper.toAuthUserFromDto(signUpRequest);
    authUser = authUseCases.signUp(authUser, signUpRequest.confirmPassword());
    return new ResponseEntity<>(userMapper.toSignUpResponseDto(authUser), HttpStatus.OK);
  }

  @Operation(
      summary = "Activate account",
      description = "Activate a user account by providing the activation code.")
  @PostMapping("/activate-account/{code}")
  public ResponseEntity<Void> activateAccount(
      @PathVariable
          @Min(value = 6, message = "Code must be at least {] characters long" + 6)
          @NotBlank(message = "Code is required")
          String code)
      throws MessagingException {
    authUseCases.activateAccount(code);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(
      summary = "Send activation code",
      description = "Send an account activation code to the user's email.")
  @PostMapping("/send-activation-code")
  public ResponseEntity<Void> sendActivationCode(@RequestParam String code)
      throws MessagingException {
    authUseCases.sendActivationCode(code);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(
      summary = "Send reset password code",
      description = "Send a reset password code to the user's email for password recovery.")
  @PostMapping("/forget-password")
  public ResponseEntity<Void> forgetPassword(@RequestParam String email) throws MessagingException {
    authUseCases.forgetPassword(email);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(
      summary = "Reset password",
      description = "Reset the user's password using a reset code and the new password details.")
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto)
      throws MessagingException {
    authUseCases.resetPassword(
        resetPasswordDto.code(), resetPasswordDto.password(), resetPasswordDto.confirmPassword());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(
      summary = "Set password",
      description =
          "Set the password for a user account using the activation code and the new password details.")
  @PostMapping("/set-password")
  public ResponseEntity<Void> setPassword(@RequestBody SetPasswordDto setPasswordDto)
      throws MessagingException {
    authUseCases.resetPassword(
        setPasswordDto.code(), setPasswordDto.password(), setPasswordDto.confirmPassword());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(
      summary = "Login",
      description = "Authenticate a user with their email and password, returning an access token.")
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody SignInDto request) {
    return new ResponseEntity<>(
        authUseCases.signIn(request.email(), request.password()), HttpStatus.OK);
  }

  @Operation(
      summary = "Refresh token",
      description = "Refresh the access token using the refresh token.")
  @PostMapping("/refresh-token")
  public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequestDto request) {
    return new ResponseEntity<>(authUseCases.refreshToken(request.refreshToken()), HttpStatus.OK);
  }

  @Operation(
      summary = "Change password",
      description =
          "Allow a logged-in user to change their password by providing the old and new passwords.")
  @PostMapping("/change-password")
  public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
    UUID id = getCurrentAuthenticatedUserId();
    authUseCases.changePassword(
        id,
        changePasswordDto.oldPassword(),
        changePasswordDto.newPassword(),
        changePasswordDto.confirmPassword());
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
