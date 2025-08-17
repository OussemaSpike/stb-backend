package com.pfe.stb.user.model;

public enum AccountStatus {
  ACTIVE, // Can transact
  SUSPENDED, // Temporarily blocked by an admin
  CLOSED // Permanently closed
}
