package com.pfe.stb.auth.repository;

import com.pfe.stb.auth.model.AuthCode;
import com.pfe.stb.auth.model.enums.CodeStatus;
import com.pfe.stb.auth.model.enums.CodeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<AuthCode, UUID> {

  Optional<AuthCode> findByCode(String code);

  Optional<AuthCode> findByUserId(UUID userId);

  List<AuthCode> findByUserIdAndStatusAndType(UUID userId, CodeStatus status, CodeType type);
}
