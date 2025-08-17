package com.pfe.stb.transfer.controller;

import com.pfe.stb.shared.security.AuthUtils;
import com.pfe.stb.transfer.dto.request.CreateBeneficiaryRequest;
import com.pfe.stb.transfer.dto.response.BeneficiaryDto;
import com.pfe.stb.transfer.port.BeneficiaryUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/beneficiaries")
@Tag(name = "Beneficiaries", description = "Operations related to managing transfer beneficiaries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENT')")
public class BeneficiaryController {

    private final BeneficiaryUseCases beneficiaryUseCases;

    @Operation(
        summary = "Create a new beneficiary",
        description = "Add a new beneficiary (2FA validation temporarily disabled).")
    @PostMapping
    public ResponseEntity<BeneficiaryDto> createBeneficiary(
            @Valid @RequestBody CreateBeneficiaryRequest request) {
        UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
        // Pass empty string for validation code since 2FA is disabled
        BeneficiaryDto beneficiary = beneficiaryUseCases.createBeneficiary(userId, request, "");
        return ResponseEntity.ok(beneficiary);
    }

    @Operation(
        summary = "Get all user beneficiaries",
        description = "Retrieve all active beneficiaries for the current user.")
    @GetMapping
    public ResponseEntity<List<BeneficiaryDto>> getUserBeneficiaries() {
        UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
        List<BeneficiaryDto> beneficiaries = beneficiaryUseCases.getUserBeneficiaries(userId);
        return ResponseEntity.ok(beneficiaries);
    }

    @Operation(
        summary = "Get beneficiary by ID",
        description = "Retrieve a specific beneficiary by ID.")
    @GetMapping("/{beneficiaryId}")
    public ResponseEntity<BeneficiaryDto> getBeneficiaryById(@PathVariable UUID beneficiaryId) {
        UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
        BeneficiaryDto beneficiary = beneficiaryUseCases.getBeneficiaryById(userId, beneficiaryId);
        return ResponseEntity.ok(beneficiary);
    }

    @Operation(
        summary = "Delete beneficiary",
        description = "Soft delete a beneficiary (sets active status to false).")
    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<Void> deleteBeneficiary(@PathVariable UUID beneficiaryId) {
        UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
        beneficiaryUseCases.deleteBeneficiary(userId, beneficiaryId);
        return ResponseEntity.noContent().build();
    }
}
