package com.pfe.stb.user.service;

import static com.pfe.stb.shared.DateUtils.convertDaysToSeconds;
import static java.time.Instant.now;

import com.pfe.stb.auth.model.AuthCode;
import com.pfe.stb.auth.model.enums.CodeStatus;
import com.pfe.stb.auth.model.enums.CodeType;
import com.pfe.stb.auth.port.output.Codes;
import com.pfe.stb.auth.service.AuthService;
import com.pfe.stb.exception.BadRequestException;
import com.pfe.stb.exception.NotFoundException;
import com.pfe.stb.notifications.model.enums.EmailType;
import com.pfe.stb.notifications.port.Emails;
import com.pfe.stb.user.dto.request.CreateClientRequest;
import com.pfe.stb.user.dto.request.UpdateProfileRequest;
import com.pfe.stb.user.dto.response.BankAccountDto;
import com.pfe.stb.user.model.BankAccount;
import com.pfe.stb.user.model.Role;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import com.pfe.stb.user.port.UserUseCases;
import com.pfe.stb.user.repository.RoleRepository;
import com.pfe.stb.user.repository.UserRepository;
import com.pfe.stb.user.utils.RibGenerator;
import jakarta.mail.MessagingException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService implements UserUseCases {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final Emails emails;
  private final Codes codes;

  @Override
  public User findById(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }

  public User createClient(CreateClientRequest request) {
    User user = new User();
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setCin(request.cin());
    user.setEmail(request.email());
    user.setPhoneNumber(request.phone());
    user.setEnabled(true);
    user.setAccountLocked(false);
    user.setEmailVerified(false);

    Role clientRole = roleRepository.findByName(RoleType.CLIENT);
    user.setRoles(Set.of(clientRole));

    BankAccount bankAccount = new BankAccount();

    // Generate RIB and set all related fields
    RibGenerator.generateAndSetRibFields(bankAccount, request.branchCode());

    bankAccount.setAccountType(request.accountType());
    bankAccount.setUser(user);

    bankAccount.setBalance(new BigDecimal("1000.00"));

    user.setBankAccount(bankAccount);

    user = userRepository.save(user);

    try {
      String code = generateCode(user.getId(), user.getEmail());
      sendEmail(user.getEmail(), code);
      log.info("Password setup email sent to user: {}", user.getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send password setup email to user: {}", user.getEmail(), e);
    }

    return user;
  }

  @Override
  public Page<User> findAll(final String search, final RoleType role, final Pageable pageable) {
    return userRepository.findAll(UserSpecifications.hasCriteria(search, role), pageable);
  }

  private void sendEmail(String email, String code) throws MessagingException {
    Map<String, Object> properties =
        Map.of(
            "username", email,
            "authCode", code,
            "url", EmailType.SET_PASSWORD.getFullPath());
    emails.sendEmail(email, EmailType.SET_PASSWORD, properties);
  }

  private String generateCode(UUID userId, String email) {
    var code = AuthService.generateRandomCode(6);
    AuthCode authCodeToSave =
        AuthCode.builder()
            .code(code)
            .type(CodeType.SET_PASSWORD)
            .email(email)
            .status(CodeStatus.PENDING)
            .expireAt(now().plusSeconds(convertDaysToSeconds(2)))
            .userId(userId)
            .build();
    codes.save(authCodeToSave);
    return code;
  }

  @Override
  public User updateProfile(UUID userId, UpdateProfileRequest request) {
    User user = findById(userId);

    // Update first name
    if (request.firstName() != null && !request.firstName().trim().isEmpty()) {
      user.setFirstName(request.firstName().trim());
      log.info("Updated first name for user: {}", userId);
    }

    // Update last name
    if (request.lastName() != null && !request.lastName().trim().isEmpty()) {
      user.setLastName(request.lastName().trim());
      log.info("Updated last name for user: {}", userId);
    }

    // Update phone number
    if (request.phoneNumber() != null && !request.phoneNumber().trim().isEmpty()) {
      user.setPhoneNumber(request.phoneNumber().trim());
      log.info("Updated phone number for user: {}", userId);
    }

    // Update address
    if (request.address() != null && !request.address().trim().isEmpty()) {
      user.setAddress(request.address().trim());
      log.info("Updated address for user: {}", userId);
    }

    return userRepository.save(user);
  }

  @Override
  public BankAccountDto getBankAccountInfo(UUID userId) {
    User user = findById(userId);
    BankAccount bankAccount = user.getBankAccount();

    if (bankAccount == null) {
      throw new NotFoundException(NotFoundException.NotFoundExceptionType.BANK_ACCOUNT_NOT_FOUND);
    }

    return BankAccountDto.builder()
        .id(bankAccount.getId())
        .rib(bankAccount.getRib())
        .accountType(bankAccount.getAccountType())
        .currency(bankAccount.getCurrency())
        .balance(formatAmount(bankAccount.getBalance()))
        .availableBalance(formatAmount(bankAccount.getAvailableBalance()))
        .status(bankAccount.getStatus())
        .build();
  }

  @Override
  public void deleteClientById(final UUID id) {
    User user = findById(id);

    userRepository.deleteById(id);
  }

  private String formatAmount(BigDecimal amount) {
    if (amount == null) {
      return null;
    }
    // Format "12345.678"
    return String.format("%.3f", amount);
  }

  @Override
  @Transactional
  public User enableClient(UUID userId) {
    User user = findById(userId);

    // Ensure user is a client
    boolean isClient = user.getRoles().stream().anyMatch(role -> role.getName() == RoleType.CLIENT);

    if (!isClient) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.INVALID_USER_ROLE_FOR_OPERATION);
    }

    if (user.isEnabled()) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.USER_ALREADY_ENABLED);
    }

    user.setEnabled(true);
    user = userRepository.save(user);

    log.info("Client {} has been enabled by admin", userId);
    return user;
  }

  @Override
  @Transactional
  public User disableClient(UUID userId) {
    User user = findById(userId);

    // Ensure user is a client
    boolean isClient = user.getRoles().stream().anyMatch(role -> role.getName() == RoleType.CLIENT);

    if (!isClient) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.INVALID_USER_ROLE_FOR_OPERATION);
    }

    if (!user.isEnabled()) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.USER_ALREADY_DISABLED);
    }

    user.setEnabled(false);
    user = userRepository.save(user);

    log.info("Client {} has been disabled by admin", userId);
    return user;
  }
}
