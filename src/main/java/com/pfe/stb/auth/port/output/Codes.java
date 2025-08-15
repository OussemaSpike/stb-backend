package com.pfe.stb.auth.port.output;

import com.pfe.stb.auth.model.AuthCode;
import com.pfe.stb.auth.model.enums.CodeStatus;
import com.pfe.stb.auth.model.enums.CodeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Codes {

  void save(AuthCode authCode);

  void saveAll(List<AuthCode> authCodes);

  Optional<AuthCode> findByCode(String code);

  void update(AuthCode authCode);

  AuthCode findByUserId(UUID userId);

  List<AuthCode> findByUserIdAndStatus(UUID userId, CodeStatus status, CodeType type);

  AuthCode deleteByCode(UUID code);
}
