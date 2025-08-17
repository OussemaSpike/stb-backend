package com.pfe.stb.transfer.service;

import com.pfe.stb.exception.BadRequestException;
import com.pfe.stb.exception.NotFoundException;
import com.pfe.stb.notifications.service.TransferNotificationService;
import com.pfe.stb.transfer.dto.request.InitiateTransferRequest;
import com.pfe.stb.transfer.dto.request.ValidateTransferRequest;
import com.pfe.stb.transfer.dto.response.TransferDto;
import com.pfe.stb.transfer.dto.response.TransferSummaryDto;
import com.pfe.stb.transfer.model.Beneficiary;
import com.pfe.stb.transfer.model.Transfer;
import com.pfe.stb.transfer.model.TransferStatus;
import com.pfe.stb.transfer.port.TransferUseCases;
import com.pfe.stb.transfer.repository.BeneficiaryRepository;
import com.pfe.stb.transfer.repository.TransferRepository;
import com.pfe.stb.transfer.specification.TransferSpecification;
import com.pfe.stb.user.model.BankAccount;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.repository.BankAccountRepository;
import com.pfe.stb.user.repository.UserRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class TransferService implements TransferUseCases {

  private final UserRepository userRepository;
  private final BankAccountRepository bankAccountRepository;
  private final BeneficiaryRepository beneficiaryRepository;
  private final TransferRepository transferRepository;
  private final TransferNotificationService transferNotificationService;
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  @Transactional
  public TransferSummaryDto initiateTransfer(UUID userId, InitiateTransferRequest request) {
    User user = getUserById(userId);
    BankAccount fromAccount = user.getBankAccount();

    if (fromAccount == null) {
      throw new BadRequestException(BadRequestException.BadRequestExceptionType.NO_BANK_ACCOUNT);
    }

    // Get beneficiary
    Beneficiary beneficiary =
        beneficiaryRepository
            .findById(request.beneficiaryId())
            .filter(b -> b.getUser().getId().equals(userId) && b.getIsActive())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.BENEFICIARY_NOT_FOUND));

    // Validate transfer amount and limits
    validateTransferLimits(fromAccount, request.amount());

    // Calculate fees (for now, no fees)
    BigDecimal fees = BigDecimal.ZERO;
    BigDecimal totalAmount = request.amount().add(fees);

    // Create transfer
    String reference = generateTransferReference();
    Transfer transfer =
        Transfer.builder()
            .reference(reference)
            .fromAccount(fromAccount)
            .beneficiary(beneficiary)
            .user(user)
            .amount(request.amount())
            .currency("TND")
            .reason(request.reason())
            .fees(fees)
            .totalAmount(totalAmount)
            .status(TransferStatus.PENDING)
            .build();

    transfer = transferRepository.save(transfer);

    log.info("Transfer initiated: {} for user {}", reference, userId);

    // Send notifications to admins about the new transfer
    transferNotificationService.notifyNewTransfer(transfer);

    return TransferSummaryDto.builder()
        .transferId(transfer.getId())
        .reference(transfer.getReference())
        .fromAccountRib(fromAccount.getRib())
        .beneficiaryName(beneficiary.getName())
        .beneficiaryRib(beneficiary.getRib())
        .amount(transfer.getAmount())
        .currency(transfer.getCurrency())
        .reason(transfer.getReason())
        .fees(transfer.getFees())
        .totalAmount(transfer.getTotalAmount())
        .initiatedAt(transfer.getCreatedAt())
        .build();
  }

  // Helper methods
  private User getUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }

  private BankAccount findBankAccountByRib(String rib) {
    return userRepository.findByBankAccount_Rib(rib).map(User::getBankAccount).orElse(null);
  }

  private void validateTransferLimits(BankAccount account, BigDecimal amount) {
    // Check if account can transfer
    if (!account.canTransfer(amount)) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.TRANSFER_LIMIT_EXCEEDED);
    }
  }

  private String generateTransferReference() {
    return "STB" + System.currentTimeMillis() + String.format("%04d", secureRandom.nextInt(10000));
  }

  private TransferDto mapToTransferDto(Transfer transfer) {
    return new TransferDto(
        transfer.getId(),
        transfer.getReference(),
        transfer.getFromAccount().getRib(),
        transfer.getBeneficiary().getName(),
        transfer.getBeneficiary().getRib(),
        transfer.getAmount(),
        transfer.getCurrency(),
        transfer.getReason(),
        transfer.getStatus(),
        transfer.getFees(),
        transfer.getTotalAmount(),
        transfer.getCreatedAt(),
        transfer.getExecutionDate(),
        transfer.getRejectionReason());
  }

  // Placeholder implementations for remaining methods
  @Override
  @Transactional
  public TransferDto validateAndExecuteTransfer(UUID userId, ValidateTransferRequest request) {
    User user = getUserById(userId);

    // Find the transfer
    Transfer transfer =
        transferRepository
            .findById(request.transferId())
            .filter(t -> t.getUser().getId().equals(userId))
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.TRANSFER_NOT_FOUND));

    // Check if transfer can be executed
    if (!transfer.getStatus().equals(TransferStatus.PENDING)) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.INVALID_TRANSFER_STATUS);
    }

    // Get user's bank account
    BankAccount userAccount = user.getBankAccount();
    if (userAccount == null) {
      throw new BadRequestException(BadRequestException.BadRequestExceptionType.NO_BANK_ACCOUNT);
    }

    // Check if account has sufficient balance (final check)
    if (userAccount.getBalance().compareTo(transfer.getTotalAmount()) < 0) {
      transfer.setStatus(TransferStatus.FAILED);
      transfer.setFailureReason("Insufficient balance");
      transferRepository.save(transfer);

      // Send notification about transfer failure
      transferNotificationService.notifyTransferFailed(transfer);

      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.INSUFFICIENT_BALANCE);
    }

    // Deduct amount from sender account
    userAccount.setBalance(userAccount.getBalance().subtract(transfer.getTotalAmount()));
    bankAccountRepository.save(userAccount); // Save sender account balance

    // Find receiver's account by RIB and add money to it
    BankAccount receiverAccount = findBankAccountByRib(transfer.getBeneficiary().getRib());
    if (receiverAccount != null) {
      receiverAccount.setBalance(receiverAccount.getBalance().add(transfer.getAmount()));
      bankAccountRepository.save(receiverAccount); // Save receiver account balance
      log.info(
          "Added {} TND to receiver account {}", transfer.getAmount(), receiverAccount.getRib());
    } else {
      log.warn(
          "Receiver account not found for RIB: {}. Transfer completed but money not credited.",
          transfer.getBeneficiary().getRib());
    }

    // Update transfer status
    transfer.setStatus(TransferStatus.COMPLETED);
    transfer.setCompletedAt(LocalDateTime.now());
    transfer.setExecutionDate(LocalDateTime.now());

    transferRepository.save(transfer);

    log.info("Transfer {} completed successfully for user {}", transfer.getReference(), userId);

    return mapToTransferDto(transfer);
  }

  @Override
  public Page<TransferDto> getUserTransfers(UUID userId, Pageable pageable) {
    User user = getUserById(userId);

    Specification<Transfer> specification = TransferSpecification.hasUser(user);

    // Apply sorting by createdAt DESC if no specific sorting is provided
    if (pageable.getSort().isUnsorted()) {
      pageable =
          PageRequest.of(
              pageable.getPageNumber(),
              pageable.getPageSize(),
              Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    Page<Transfer> transfers = transferRepository.findAll(specification, pageable);
    return transfers.map(this::mapToTransferDto);
  }

  @Override
  public Page<TransferDto> getUserTransfersWithFilters(
      UUID userId,
      TransferStatus status,
      String beneficiaryName,
      BigDecimal minAmount,
      BigDecimal maxAmount,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable) {
    User user = getUserById(userId);

    Specification<Transfer> specification =
        TransferSpecification.buildSpecification(
            user, status, null, beneficiaryName, minAmount, maxAmount, startDate, endDate);

    Page<Transfer> transfers = transferRepository.findAll(specification, pageable);
    return transfers.map(this::mapToTransferDto);
  }

  @Override
  public TransferDto getTransferById(UUID transferId) {
    Transfer transfer =
        transferRepository
            .findById(transferId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.TRANSFER_NOT_FOUND));

    return mapToTransferDto(transfer);
  }

  @Override
  public TransferDto getTransferById(UUID userId, UUID transferId) {
    getUserById(userId);

    Transfer transfer =
        transferRepository
            .findById(transferId)
            .filter(t -> t.getUser().getId().equals(userId))
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.TRANSFER_NOT_FOUND));

    return mapToTransferDto(transfer);
  }

  @Override
  public List<TransferDto> getPendingTransfers() {
    List<Transfer> pendingTransfers = transferRepository.findByStatus(TransferStatus.PENDING);
    return pendingTransfers.stream().map(this::mapToTransferDto).toList();
  }

  @Override
  public Page<TransferDto> getAllTransfersForAdmin(
      TransferStatus status,
      String search,
      String beneficiaryName,
      BigDecimal minAmount,
      BigDecimal maxAmount,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable) {

    Specification<Transfer> specification =
        TransferSpecification.buildAdminSpecification(
            status, search, beneficiaryName, minAmount, maxAmount, startDate, endDate);

    Page<Transfer> transfers = transferRepository.findAll(specification, pageable);
    return transfers.map(this::mapToTransferDto);
  }

  @Override
  @Transactional
  public TransferDto approveTransfer(UUID transferId, UUID adminId, String comment) {
    Transfer transfer =
        transferRepository
            .findById(transferId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.TRANSFER_NOT_FOUND));

    if (!transfer.getStatus().equals(TransferStatus.PENDING)) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.INVALID_TRANSFER_STATUS);
    }

    // Get sender's bank account
    BankAccount senderAccount = transfer.getFromAccount();

    // Check if account has sufficient balance
    if (senderAccount.getBalance().compareTo(transfer.getTotalAmount()) < 0) {
      transfer.setStatus(TransferStatus.FAILED);
      transfer.setFailureReason("Insufficient balance");
      transferRepository.save(transfer);

      // Send notification about transfer failure
      transferNotificationService.notifyTransferFailed(transfer);

      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.INSUFFICIENT_BALANCE);
    }

    // Execute the money transfer
    // Deduct amount from sender account
    senderAccount.setBalance(senderAccount.getBalance().subtract(transfer.getTotalAmount()));
    bankAccountRepository.save(senderAccount);

    // Find receiver's account by RIB and add money to it
    BankAccount receiverAccount = findBankAccountByRib(transfer.getBeneficiary().getRib());
    if (receiverAccount != null) {
      receiverAccount.setBalance(receiverAccount.getBalance().add(transfer.getAmount()));
      bankAccountRepository.save(receiverAccount);
      log.info(
          "Added {} TND to receiver account {} via admin approval",
          transfer.getAmount(),
          receiverAccount.getRib());
    } else {
      log.warn(
          "Receiver account not found for RIB: {} during admin approval. Money not credited.",
          transfer.getBeneficiary().getRib());
    }

    // Update transfer status
    transfer.setStatus(TransferStatus.COMPLETED);
    transfer.setApprovedBy(adminId);
    transfer.setApprovedAt(LocalDateTime.now());
    transfer.setAdminComment(comment);
    transfer.setCompletedAt(LocalDateTime.now());
    transfer.setExecutionDate(LocalDateTime.now());

    transferRepository.save(transfer);

    log.info("Transfer {} approved and completed by admin {}", transfer.getReference(), adminId);

    // Send notifications about transfer approval
    transferNotificationService.notifyTransferApproved(transfer);

    return mapToTransferDto(transfer);
  }

  @Override
  @Transactional
  public TransferDto rejectTransfer(UUID transferId, UUID adminId, String reason) {
    Transfer transfer =
        transferRepository
            .findById(transferId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.TRANSFER_NOT_FOUND));

    if (!transfer.getStatus().equals(TransferStatus.PENDING)) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.INVALID_TRANSFER_STATUS);
    }

    // Reject the transfer
    transfer.setStatus(TransferStatus.CANCELLED);
    transfer.setRejectedBy(adminId);
    transfer.setRejectedAt(LocalDateTime.now());
    transfer.setRejectionReason(reason);

    transferRepository.save(transfer);

    log.info(
        "Transfer {} rejected by admin {} with reason: {}",
        transfer.getReference(),
        adminId,
        reason);

    // Send notifications about transfer rejection
    transferNotificationService.notifyTransferRejected(transfer);

    return mapToTransferDto(transfer);
  }
}
