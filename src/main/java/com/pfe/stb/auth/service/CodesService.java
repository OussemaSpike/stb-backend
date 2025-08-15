package com.pfe.stb.auth.service;

import com.pfe.stb.auth.model.AuthCode;
import com.pfe.stb.auth.model.enums.CodeStatus;
import com.pfe.stb.auth.model.enums.CodeType;
import com.pfe.stb.auth.port.output.Codes;
import com.pfe.stb.auth.repository.CodeRepository;
import com.pfe.stb.exception.GenericException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class CodesService implements Codes {

  private static final String CODE_NOT_FOUND = "Code not found";
  private final CodeRepository codeRepository;

  @Override
  public void save(AuthCode authCode) {
    log.info("Saving code for user: {}", authCode.getUserId());
    codeRepository.save(authCode);
    log.info("Code saved successfully for user: {}", authCode.getUserId());
  }

  @Override
  public void saveAll(List<AuthCode> authCodes) {
    log.info("Saving codes");
    codeRepository.saveAll(authCodes);
    log.info("Codes saved successfully");
  }

  @Override
  public Optional<AuthCode> findByCode(String code) {
    log.info("Finding code: {}", code);
    return codeRepository
        .findByCode(code)
        .or(
            () -> {
              log.error(CODE_NOT_FOUND + ": {}", code);
              throw new GenericException(GenericException.GenericExceptionType.GENERIC);
            });
  }

  @Override
  public void update(AuthCode authCode) {
    log.info("Updating code for user: {}", authCode.getUserId());
    codeRepository.save(authCode);
    log.info("Code updated successfully for user: {}", authCode.getUserId());
  }

  @Override
  public AuthCode findByUserId(UUID userId) {
    log.info("Finding code for user: {}", userId);
    return codeRepository
        .findByUserId(userId)
        .orElseThrow(
            () -> {
              log.error("Code not found for user: {}", userId);
              return new GenericException(GenericException.GenericExceptionType.GENERIC);
            });
  }

  @Override
  public List<AuthCode> findByUserIdAndStatus(UUID userId, CodeStatus status, CodeType type) {
    log.info("Finding code for user: {} with status: {}", userId, status);
    return codeRepository.findByUserIdAndStatusAndType(userId, status, type);
  }

  @Override
  public AuthCode deleteByCode(UUID code) {
    log.info("Deleting code: {}", code);
    var codeEntity =
        codeRepository
            .findById(code)
            .orElseThrow(
                () -> {
                  log.error(CODE_NOT_FOUND + ": {}", code);
                  return new GenericException(GenericException.GenericExceptionType.GENERIC);
                });
    codeRepository.delete(codeEntity);
    log.info("Code deleted successfully: {}", code);
    return codeEntity;
  }
}
