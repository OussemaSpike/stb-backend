package com.pfe.stb.user.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Nullable 
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    String firstName,
    
    @Nullable 
    @Size(min = 2, max = 50, message = "Le nom de famille doit contenir entre 2 et 50 caractères")
    String lastName,
    
    @Nullable 
    @Size(min = 8, max = 15, message = "Le numéro de téléphone doit contenir entre 8 et 15 caractères")
    String phoneNumber,
    
    @Nullable 
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    String address
) {}
