package com.pfe.stb.transfer.repository;

import com.pfe.stb.transfer.model.Beneficiary;
import com.pfe.stb.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    
    List<Beneficiary> findByUserAndIsActiveTrue(User user);
    
    Optional<Beneficiary> findByUserAndRib(User user, String rib);
    
    @Query("SELECT b FROM Beneficiary b WHERE b.user = :user AND b.isActive = true ORDER BY b.name ASC")
    List<Beneficiary> findActiveByUserOrderByName(@Param("user") User user);
    
    boolean existsByUserAndRib(User user, String rib);
}
