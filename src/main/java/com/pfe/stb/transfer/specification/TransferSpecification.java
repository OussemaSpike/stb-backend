package com.pfe.stb.transfer.specification;

import com.pfe.stb.transfer.model.Transfer;
import com.pfe.stb.transfer.model.TransferStatus;
import com.pfe.stb.user.model.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransferSpecification {

    private TransferSpecification() {
        // Utility class
    }

    public static Specification<Transfer> hasStatus(TransferStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Transfer> hasUser(User user) {
        return (root, query, criteriaBuilder) -> {
            if (user == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("user"), user);
        };
    }

    public static Specification<Transfer> searchByKeyword(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }
            
            String searchPattern = "%" + search.toLowerCase() + "%";
            
            // Join with beneficiary and user tables
            Join<Transfer, Object> beneficiaryJoin = root.join("beneficiary", JoinType.LEFT);
            Join<Transfer, Object> fromAccountJoin = root.join("fromAccount", JoinType.LEFT);
            Join<Transfer, Object> userJoin = root.join("user", JoinType.LEFT);
            
            Predicate[] predicates = {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("reference")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(beneficiaryJoin.get("name")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(beneficiaryJoin.get("rib")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(fromAccountJoin.get("rib")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), searchPattern)
            };
            
            return criteriaBuilder.or(predicates);
        };
    }

    public static Specification<Transfer> hasBeneficiaryName(String beneficiaryName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(beneficiaryName)) {
                return null;
            }
            Join<Transfer, Object> beneficiaryJoin = root.join("beneficiary", JoinType.LEFT);
            return criteriaBuilder.like(
                criteriaBuilder.lower(beneficiaryJoin.get("name")), 
                "%" + beneficiaryName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Transfer> hasAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            if (minAmount == null && maxAmount == null) {
                return null;
            }
            
            if (minAmount != null && maxAmount != null) {
                return criteriaBuilder.between(root.get("amount"), minAmount, maxAmount);
            } else if (minAmount != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
            }
        };
    }

    public static Specification<Transfer> hasCreatedAtBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
            
            if (startDateTime != null && endDateTime != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDateTime, endDateTime);
            } else if (startDateTime != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime);
            }
        };
    }

    public static Specification<Transfer> buildSpecification(
            User user,
            TransferStatus status,
            String search,
            String beneficiaryName,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDate startDate,
            LocalDate endDate) {
        
        return Specification.where(hasUser(user))
                .and(hasStatus(status))
                .and(searchByKeyword(search))
                .and(hasBeneficiaryName(beneficiaryName))
                .and(hasAmountBetween(minAmount, maxAmount))
                .and(hasCreatedAtBetween(startDate, endDate));
    }

    public static Specification<Transfer> buildAdminSpecification(
            TransferStatus status,
            String search,
            String beneficiaryName,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDate startDate,
            LocalDate endDate) {
        
        return Specification.where(hasStatus(status))
                .and(searchByKeyword(search))
                .and(hasBeneficiaryName(beneficiaryName))
                .and(hasAmountBetween(minAmount, maxAmount))
                .and(hasCreatedAtBetween(startDate, endDate));
    }
}
