package com.pfe.stb.user.repository;

import com.pfe.stb.user.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
}
