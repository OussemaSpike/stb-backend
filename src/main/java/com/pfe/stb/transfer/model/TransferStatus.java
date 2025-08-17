package com.pfe.stb.transfer.model;

import lombok.Getter;

@Getter
public enum TransferStatus {
    PENDING("En attente"),
    COMPLETED("Terminé"),
    FAILED("Échoué"),
    CANCELLED("Annulé");

    private final String displayName;

    TransferStatus(String displayName) {
        this.displayName = displayName;
    }
}
