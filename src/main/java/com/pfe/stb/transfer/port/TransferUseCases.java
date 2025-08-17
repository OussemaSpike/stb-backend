package com.pfe.stb.transfer.port;

import com.pfe.stb.transfer.dto.request.InitiateTransferRequest;
import com.pfe.stb.transfer.dto.request.ValidateTransferRequest;
import com.pfe.stb.transfer.dto.response.TransferDto;
import com.pfe.stb.transfer.dto.response.TransferSummaryDto;
import com.pfe.stb.transfer.model.TransferStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferUseCases {

  // Transfer operations
  TransferSummaryDto initiateTransfer(UUID userId, InitiateTransferRequest request);

  TransferDto validateAndExecuteTransfer(UUID userId, ValidateTransferRequest request);

  // Transfer history and tracking
  Page<TransferDto> getUserTransfers(UUID userId, Pageable pageable);

  Page<TransferDto> getUserTransfersWithFilters(
      UUID userId,
      TransferStatus status,
      String beneficiaryName,
      BigDecimal minAmount,
      BigDecimal maxAmount,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable);

  TransferDto getTransferById(UUID transferId);

  TransferDto getTransferById(UUID userId, UUID transferId);

  // Admin operations (for back-office)
  List<TransferDto> getPendingTransfers();

  Page<TransferDto> getAllTransfersForAdmin(
      TransferStatus status,
      String search,
      String beneficiaryName,
      BigDecimal minAmount,
      BigDecimal maxAmount,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable);

  TransferDto approveTransfer(UUID transferId, UUID adminId, String comment);

  TransferDto rejectTransfer(UUID transferId, UUID adminId, String reason);
}
