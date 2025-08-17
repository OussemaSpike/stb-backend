package com.pfe.stb.transfer.port;

import com.pfe.stb.transfer.dto.request.CreateBeneficiaryRequest;
import com.pfe.stb.transfer.dto.response.BeneficiaryDto;
import java.util.List;
import java.util.UUID;

public interface BeneficiaryUseCases {
    
    BeneficiaryDto createBeneficiary(UUID userId, CreateBeneficiaryRequest request, String validationCode);
    
    List<BeneficiaryDto> getUserBeneficiaries(UUID userId);
    
    BeneficiaryDto getBeneficiaryById(UUID userId, UUID beneficiaryId);
    
    void deleteBeneficiary(UUID userId, UUID beneficiaryId);
}
