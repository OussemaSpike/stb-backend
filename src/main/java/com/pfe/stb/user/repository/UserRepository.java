package com.pfe.stb.user.repository;

import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmail(String email);

  List<User> findAllByAccountLockedTrue();
  
  Optional<User> findByBankAccount_Rib(String rib);
  
  List<User> findByRoles_Name(RoleType roleType);
  
  // Dashboard statistics methods
  Long countByEnabledTrue();
  
  Long countByEnabledFalse();
}
