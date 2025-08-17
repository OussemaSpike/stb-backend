package com.pfe.stb.user.utils;

import com.pfe.stb.user.model.BankAccount;
import java.security.SecureRandom;

public class RibGenerator {

  private static final SecureRandom random = new SecureRandom();
  private static final String STB_BANK_CODE = "08"; // STB bank code in Tunisia

  private RibGenerator() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Generates a 20-digit RIB for STB Tunisia and populates BankAccount fields Format: BB CCC
   * NNNNNNNNNNN KK BB = Bank code (08 for STB) CCC = Branch code NNNNNNNNNNN = Account number (11
   * digits) KK = Check digits
   */
  public static void generateAndSetRibFields(BankAccount bankAccount, String branchCode) {

    // Generate random account number (11 digits)
    StringBuilder accountNumber = new StringBuilder();
    for (int i = 0; i < 13; i++) {
      accountNumber.append(random.nextInt(10));
    }

    String accountNum = accountNumber.toString();

    // Calculate check digits
    String baseRib = STB_BANK_CODE + branchCode + accountNum;
    String checkDigits = calculateCheckDigits(baseRib);

    String fullRib = baseRib + checkDigits;

    // Set all the fields
    bankAccount.setRib(fullRib);
  }

  private static String calculateCheckDigits(String baseRib) {
    // Simplified check digit calculation
    long sum = 0;
    for (char c : baseRib.toCharArray()) {
      sum += Character.getNumericValue(c);
    }
    int checkSum = (int) (sum % 97);
    return String.format("%02d", checkSum);
  }
}
