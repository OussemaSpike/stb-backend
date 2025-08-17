package com.pfe.stb.transfer.controller;

import com.pfe.stb.shared.pagination.CustomPage;
import com.pfe.stb.shared.pagination.PageMapper;
import com.pfe.stb.shared.pagination.PaginationUtils;
import com.pfe.stb.shared.security.AuthUtils;
import com.pfe.stb.transfer.dto.response.TransferDto;
import com.pfe.stb.transfer.model.TransferStatus;
import com.pfe.stb.transfer.port.TransferUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/transfers")
@Tag(name = "Admin Transfers", description = "Admin operations for managing transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransferController {

  private final TransferUseCases transferUseCases;

  @Operation(
      summary = "Get all transfers",
      description =
          "Retrieve all transfers in the application with pagination, sorting, and filtering options. Supports sorting by fields and filtering based on criteria.")
  @GetMapping
  public ResponseEntity<CustomPage<TransferDto>> getAllTransfers(
      @RequestParam(defaultValue = "0", required = false) int page,
      @RequestParam(defaultValue = "10", required = false) int size,
      @RequestParam(defaultValue = "createdAt", required = false) String sort,
      @RequestParam(defaultValue = "DESC", required = false) String sortDirection,
      @RequestParam(defaultValue = "", required = false) String search,
      @RequestParam(required = false) TransferStatus status,
      @RequestParam(required = false) String beneficiaryName,
      @RequestParam(required = false) BigDecimal minAmount,
      @RequestParam(required = false) BigDecimal maxAmount,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {

    Pageable pageable = PaginationUtils.createPageable(page, size, sort, sortDirection);

    Page<TransferDto> transfersPage =
        transferUseCases.getAllTransfersForAdmin(
            status, search, beneficiaryName, minAmount, maxAmount, startDate, endDate, pageable);

    return ResponseEntity.ok(PageMapper.toCustomPage(transfersPage));
  }

  @Operation(
      summary = "Get pending transfers",
      description = "Retrieve all pending transfers awaiting admin approval.")
  @GetMapping("/pending")
  public ResponseEntity<List<TransferDto>> getPendingTransfers() {
    List<TransferDto> pendingTransfers = transferUseCases.getPendingTransfers();
    return ResponseEntity.ok(pendingTransfers);
  }

  @Operation(
      summary = "Approve transfer",
      description = "Admin approves a pending transfer and executes the money transfer.")
  @PostMapping("/{transferId}/approve")
  public ResponseEntity<TransferDto> approveTransfer(
      @PathVariable UUID transferId, @RequestParam(required = false) String comment) {
    UUID adminId = AuthUtils.getCurrentAuthenticatedUserId();
    TransferDto transfer = transferUseCases.approveTransfer(transferId, adminId, comment);
    return ResponseEntity.ok(transfer);
  }

  @Operation(
      summary = "Reject transfer",
      description = "Admin rejects a pending transfer with a reason.")
  @PostMapping("/{transferId}/reject")
  public ResponseEntity<TransferDto> rejectTransfer(
      @PathVariable UUID transferId, @RequestParam String reason) {
    UUID adminId = AuthUtils.getCurrentAuthenticatedUserId();
    TransferDto transfer = transferUseCases.rejectTransfer(transferId, adminId, reason);
    return ResponseEntity.ok(transfer);
  }

  @Operation(summary = "Get transfer by ID", description = "Retrieve a specific transfer by ID.")
  @GetMapping("/{transferId}")
  public ResponseEntity<TransferDto> getTransferByIdForADMIN(@PathVariable UUID transferId) {
    TransferDto transfer = transferUseCases.getTransferById(transferId);
    return ResponseEntity.ok(transfer);
  }
}
