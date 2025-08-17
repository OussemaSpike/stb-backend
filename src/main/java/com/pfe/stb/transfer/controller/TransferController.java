package com.pfe.stb.transfer.controller;

import com.pfe.stb.shared.pagination.CustomPage;
import com.pfe.stb.shared.pagination.PageMapper;
import com.pfe.stb.shared.pagination.PaginationUtils;
import com.pfe.stb.shared.security.AuthUtils;
import com.pfe.stb.transfer.dto.request.InitiateTransferRequest;
import com.pfe.stb.transfer.dto.request.ValidateTransferRequest;
import com.pfe.stb.transfer.dto.response.TransferDto;
import com.pfe.stb.transfer.dto.response.TransferSummaryDto;
import com.pfe.stb.transfer.model.TransferStatus;
import com.pfe.stb.transfer.port.TransferUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@Tag(name = "Transfers", description = "Operations related to money transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
public class TransferController {

  private final TransferUseCases transferUseCases;

  @Operation(
      summary = "Initiate a new transfer",
      description = "Create a new transfer request. Transfer will be in PENDING status.")
  @PostMapping("/initiate")
  public ResponseEntity<TransferSummaryDto> initiateTransfer(
      @Valid @RequestBody InitiateTransferRequest request) {
    UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
    TransferSummaryDto transferSummary = transferUseCases.initiateTransfer(userId, request);
    return ResponseEntity.ok(transferSummary);
  }

  @Operation(
      summary = "Execute transfer",
      description = "Execute a pending transfer directly (no 2FA validation).")
  @PostMapping("/validate")
  public ResponseEntity<TransferDto> validateAndExecuteTransfer(
      @Valid @RequestBody ValidateTransferRequest request) {
    UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
    TransferDto transfer = transferUseCases.validateAndExecuteTransfer(userId, request);
    return ResponseEntity.ok(transfer);
  }

  @Operation(
      summary = "Get user transfer history",
      description =
          "Retrieve all user transfers with pagination, sorting, and filtering options. Supports sorting by fields and filtering based on criteria.")
  @GetMapping
  public ResponseEntity<CustomPage<TransferDto>> getUserTransfers(
      @RequestParam(defaultValue = "0", required = false) int page,
      @RequestParam(defaultValue = "10", required = false) int size,
      @RequestParam(defaultValue = "createdAt", required = false) String sort,
      @RequestParam(defaultValue = "DESC", required = false) String sortDirection,
      @RequestParam(required = false) TransferStatus status,
      @RequestParam(required = false) String beneficiaryName,
      @RequestParam(required = false) BigDecimal minAmount,
      @RequestParam(required = false) BigDecimal maxAmount,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {

    UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
    Pageable pageable = PaginationUtils.createPageable(page, size, sort, sortDirection);

    Page<TransferDto> transfersPage =
        transferUseCases.getUserTransfersWithFilters(
            userId, status, beneficiaryName, minAmount, maxAmount, startDate, endDate, pageable);

    return ResponseEntity.ok(PageMapper.toCustomPage(transfersPage));
  }

  @Operation(summary = "Get transfer by ID", description = "Retrieve a specific transfer by ID.")
  @GetMapping("/{transferId}")
  public ResponseEntity<TransferDto> getTransferById(@PathVariable UUID transferId) {
    UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
    TransferDto transfer = transferUseCases.getTransferById(userId, transferId);
    return ResponseEntity.ok(transfer);
  }
}
