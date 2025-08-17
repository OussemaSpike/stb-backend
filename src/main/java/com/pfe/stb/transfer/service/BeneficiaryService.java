package com.pfe.stb.transfer.service;

import com.pfe.stb.exception.BadRequestException;
import com.pfe.stb.exception.NotFoundException;
import com.pfe.stb.transfer.dto.request.CreateBeneficiaryRequest;
import com.pfe.stb.transfer.dto.response.BeneficiaryDto;
import com.pfe.stb.transfer.model.Beneficiary;
import com.pfe.stb.transfer.port.BeneficiaryUseCases;
import com.pfe.stb.transfer.repository.BeneficiaryRepository;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class BeneficiaryService implements BeneficiaryUseCases {

  private final UserRepository userRepository;
  private final BeneficiaryRepository beneficiaryRepository;

  @Override
  @Transactional
  public BeneficiaryDto createBeneficiary(
      UUID userId, CreateBeneficiaryRequest request, String validationCode) {
    User user = getUserById(userId);

    // Check if beneficiary already exists
    if (beneficiaryRepository.existsByUserAndRib(user, request.rib())) {
      throw new BadRequestException(
          BadRequestException.BadRequestExceptionType.BENEFICIARY_ALREADY_EXISTS);
    }

    Beneficiary beneficiary =
        Beneficiary.builder()
            .name(request.name())
            .rib(request.rib())
            .user(user)
            .isVerified(true) // Auto-verified (no 2FA)
            .isActive(true)
            .build();

    beneficiary = beneficiaryRepository.save(beneficiary);
    log.info("New beneficiary created for user {}: {}", userId, beneficiary.getName());

    return mapToBeneficiaryDto(beneficiary);
  }

  @Override
  public List<BeneficiaryDto> getUserBeneficiaries(UUID userId) {
    User user = getUserById(userId);
    return beneficiaryRepository.findActiveByUserOrderByName(user).stream()
        .map(this::mapToBeneficiaryDto)
        .toList();
  }

  @Override
  public BeneficiaryDto getBeneficiaryById(UUID userId, UUID beneficiaryId) {
    User user = getUserById(userId);
    Beneficiary beneficiary =
        beneficiaryRepository
            .findById(beneficiaryId)
            .filter(b -> b.getUser().getId().equals(userId))
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.BENEFICIARY_NOT_FOUND));

    return mapToBeneficiaryDto(beneficiary);
  }

  @Override
  @Transactional
  public void deleteBeneficiary(UUID userId, UUID beneficiaryId) {
    User user = getUserById(userId);
    Beneficiary beneficiary =
        beneficiaryRepository
            .findById(beneficiaryId)
            .filter(b -> b.getUser().getId().equals(userId))
            .orElseThrow(
                () ->
                    new NotFoundException(
                        NotFoundException.NotFoundExceptionType.BENEFICIARY_NOT_FOUND));

    // Soft delete by setting isActive to false
    beneficiary.setIsActive(false);
    beneficiaryRepository.save(beneficiary);
    log.info("Beneficiary {} deactivated for user {}", beneficiaryId, userId);
  }

  // Helper methods
  private User getUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }

  private BeneficiaryDto mapToBeneficiaryDto(Beneficiary beneficiary) {
    return new BeneficiaryDto(
        beneficiary.getId(),
        beneficiary.getName(),
        beneficiary.getRib(),
        beneficiary.getIsVerified(),
        beneficiary.getIsActive());
  }
}
