package com.pfe.stb.auth.service;

import static com.pfe.stb.auth.model.enums.CodeType.RESET_PASSWORD;
import static com.pfe.stb.auth.model.enums.CodeType.SET_PASSWORD;
import static com.pfe.stb.auth.utils.TokenUtils.createClaims;
import static com.pfe.stb.auth.utils.TokenUtils.createRefreshClaims;
import static com.pfe.stb.notifications.model.enums.EmailType.ACCOUNT_CONFIRMATION;
import static com.pfe.stb.shared.DateUtils.convertDaysToSeconds;
import static com.pfe.stb.shared.DateUtils.isInPast;
import static java.time.Instant.now;

import com.pfe.stb.auth.model.AuthCode;
import com.pfe.stb.auth.model.AuthResponse;
import com.pfe.stb.auth.model.enums.CodeStatus;
import com.pfe.stb.auth.model.enums.CodeType;
import com.pfe.stb.auth.port.input.AuthUseCases;
import com.pfe.stb.auth.port.output.Codes;
import com.pfe.stb.exception.BadRequestException;
import com.pfe.stb.exception.ExistsException;
import com.pfe.stb.exception.GenericException;
import com.pfe.stb.exception.NotFoundException;
import com.pfe.stb.notifications.model.enums.EmailType;
import com.pfe.stb.notifications.port.Emails;
import com.pfe.stb.security.JwtService;
import com.pfe.stb.user.model.Role;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import com.pfe.stb.user.repository.RoleRepository;
import com.pfe.stb.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService implements AuthUseCases {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final Emails emails;
  private final Codes codes;

  public static String generateRandomCode(int length) {
    String characters = "0123456789";
    StringBuilder codeBuilder = new StringBuilder();
    SecureRandom random = new SecureRandom();
    for (int i = 0; i < length; i++) {
      int randomIndex = random.nextInt(characters.length());
      codeBuilder.append(characters.charAt(randomIndex));
    }
    return codeBuilder.toString();
  }

  @Override
  @Transactional
  public User signUp(User user, String confirmPassword) throws MessagingException {
    checkPassword(user.getPassword(), confirmPassword);

    if (user.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    Role roleEntity = roleRepository.findByName(RoleType.USER);
    user.setEnabled(true);
    user.setRoles(Set.of(roleEntity));
    User savedUser;
    try {
      savedUser = userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      throw new ExistsException(
          ExistsException.ExistsExceptionType.EMAIL_ALREADY_EXISTS, e.getCause());
    }

    String code = generateCode(savedUser.getId(), savedUser.getEmail(), CodeType.ACTIVATE_ACCOUNT);
    sendEmail(savedUser.getEmail(), code, ACCOUNT_CONFIRMATION);

    return savedUser;
  }

  @Override
  @Transactional
  public void activateAccount(String receivedCode) {
    AuthCode authCode = checkCode(receivedCode);
    User user = checkUserById(authCode.getUserId());

    if (isInPast(authCode.getExpireAt())) {
      throw new NotFoundException(
          NotFoundException.NotFoundExceptionType.CODE_EXPIRED, receivedCode);
    }

    // --- Start: Logic from AuthJpaAdapter.activateAccount ---
    log.info("Activating account for user: {}", user.getEmail());
    user.setEnabled(true);
    user.setEmailVerified(true);
    userRepository.save(user);
    authCode.setStatus(CodeStatus.USED);
    codes.save(authCode);
    log.info("Account activated for user: {}", user.getEmail());
    // --- End: Logic from AuthJpaAdapter.activateAccount ---

    // Mark all other activation tokens for the user as used
    List<AuthCode> userTokens =
        codes.findByUserIdAndStatus(user.getId(), CodeStatus.PENDING, CodeType.ACTIVATE_ACCOUNT);
    for (AuthCode token : userTokens) {
      token.setStatus(CodeStatus.USED);
    }
    codes.saveAll(userTokens);
  }

  @Override
  public void sendActivationCode(String code) throws MessagingException {
    AuthCode authCode =
        codes
            .findByCode(code)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.CODE_NOT_FOUND, code));
    User user = checkUserById(authCode.getUserId());
    if (user.isEmailVerified()) {
      throw new ExistsException(ExistsException.ExistsExceptionType.EMAIL_ALREADY_VERIFIED);
    }

    if (isInPast(authCode.getExpireAt())) {
      // Generate a new code if the old one is expired
      String newCode = generateCode(user.getId(), user.getEmail(), CodeType.ACTIVATE_ACCOUNT);
      sendEmail(user.getEmail(), newCode, ACCOUNT_CONFIRMATION);
    } else {
      // Resend the existing code if it is still valid
      sendEmail(user.getEmail(), authCode.getCode(), ACCOUNT_CONFIRMATION);
    }
  }

  @Override
  public void forgetPassword(String email) throws MessagingException {
    var user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.USER_NOT_FOUND, email));
    String code = generateCode(user.getId(), user.getEmail(), RESET_PASSWORD);
    sendEmail(email, code, EmailType.RESET_PASSWORD);
  }

  @Override
  @Transactional
  public void resetPassword(String code, String password, String confirmPassword)
      throws MessagingException {
    checkPassword(password, confirmPassword);
    AuthCode authCode = checkCode(code);
    User user = checkUserById(authCode.getUserId());

    if (isInPast(authCode.getExpireAt())) {
      if (user.getPassword() == null) {
        String newCode = generateCode(user.getId(), user.getEmail(), SET_PASSWORD);
        sendEmail(user.getEmail(), newCode, EmailType.SET_PASSWORD);
      } else {
        throw new NotFoundException(NotFoundException.NotFoundExceptionType.CODE_EXPIRED, code);
      }
      return;
    }

    changeUserPassword(user, password);
    authCode.setStatus(CodeStatus.USED);
    codes.save(authCode);
  }

  @Override
  public AuthResponse signIn(String email, String password) {
    log.info("Signing in user: {}", email);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    userRepository.save(user);
    return this.createAccessToken(user);
  }

  @Override
  public AuthResponse refreshToken(String refreshToken) {
    String userId = jwtService.extractUsername(refreshToken);
    boolean isRefreshToken = jwtService.extractIsRefreshToken(refreshToken);
    if (!jwtService.isRefreshTokenValid(refreshToken, userId) || !isRefreshToken) {
      throw new BadRequestException(BadRequestException.BadRequestExceptionType.INVALID_TOKEN);
    }
    User user =
        userRepository
            .findById(UUID.fromString(userId))
            .orElseThrow(
                () ->
                    new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
    return this.createAccessToken(user);
  }

  @Override
  @Transactional
  public void changePassword(
      UUID id, String oldPassword, String newPassword, String confirmPassword) {
    User user = checkUserById(id);

    if (user.getPassword() == null) {
      throw new BadRequestException(BadRequestException.BadRequestExceptionType.NO_PASSWORD);
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(user.getEmail(), oldPassword));
    } catch (Exception e) {
      throw new BadRequestException(BadRequestException.BadRequestExceptionType.WRONG_PASSWORD);
    }
    checkPassword(newPassword, confirmPassword);

    changeUserPassword(user, newPassword);
  }

  private AuthResponse createAccessToken(User user) {
    Map<String, Object> claims = createClaims(user);
    Map<String, Object> refreshClaims = createRefreshClaims(user);
    var newToken = jwtService.generateToken(claims, user);
    var newRefreshToken = jwtService.generateRefreshToken(refreshClaims, user);
    return AuthResponse.builder().accessToken(newToken).refreshToken(newRefreshToken).build();
  }

  private void changeUserPassword(User user, String newPassword) {
    log.info("Changing password for user: {}", user.getEmail());
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    log.info("Password changed successfully for user: {}", user.getEmail());
  }

  private void sendEmail(String email, String code, EmailType emailType) throws MessagingException {
    Map<String, Object> properties =
        Map.of(
            "username", email,
            "authCode", code,
            "url", emailType.getFullPath());
    emails.sendEmail(email, emailType, properties);
  }

  private String generateCode(UUID userId, String email, CodeType type) {
    var code = generateRandomCode(6);
    AuthCode authCodeToSave =
        AuthCode.builder()
            .code(code)
            .type(type)
            .email(email)
            .status(CodeStatus.PENDING)
            .expireAt(now().plusSeconds(convertDaysToSeconds(2)))
            .userId(userId)
            .build();
    codes.save(authCodeToSave);
    return code;
  }

  private User checkUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    NotFoundException.NotFoundExceptionType.USER_NOT_FOUND, userId));
  }

  private void checkPassword(String password, String confirmPassword) {
    if (password == null || !password.equals(confirmPassword)) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.PASSWORDS_DO_NOT_MATCH);
    }
  }

  private AuthCode checkCode(String receivedCode) {
    var code =
        codes
            .findByCode(receivedCode)
            .orElseThrow(() -> new GenericException(GenericException.GenericExceptionType.GENERIC));
    if (code.getStatus().equals(CodeStatus.USED)) {
      throw new GenericException(GenericException.GenericExceptionType.GENERIC);
    }
    return code;
  }
}
