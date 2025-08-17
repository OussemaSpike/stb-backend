package com.pfe.stb.user.model;

import lombok.Getter;

@Getter
public enum BankAccountType {
  COMPTE_COURANT("Compte Courant"),
  COMPTE_EPARGNE("Compte d'Épargne"),
  COMPTE_TERME("Compte à Terme"),
  COMPTE_PROFESSIONNEL("Compte Professionnel"),
  COMPTE_DEVISE("Compte Devise");

  private final String displayName;

  BankAccountType(String displayName) {
    this.displayName = displayName;
  }
}
