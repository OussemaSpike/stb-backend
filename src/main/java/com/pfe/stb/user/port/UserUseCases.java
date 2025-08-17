package com.pfe.stb.user.port;

import com.pfe.stb.user.dto.request.CreateClientRequest;
import com.pfe.stb.user.dto.request.UpdateProfileRequest;
import com.pfe.stb.user.dto.response.BankAccountDto;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Interface defining the use cases for managing users. */
public interface UserUseCases {

  /**
   * Finds a user by their unique identifier.
   *
   * @param id the unique identifier of the user
   * @return the user associated with the given id
   */
  User findById(UUID id);

  /**
   * Creates a new client user (admin operation).
   *
   * @param request the user creation request
   * @return the created user
   */
  User createClient(CreateClientRequest request);

  Page<User> findAll(String search, RoleType role, Pageable pageable);

  /**
   * Updates a user's profile information (address, phone).
   *
   * @param userId the user ID
   * @param request the profile update request
   * @return the updated user
   */
  User updateProfile(UUID userId, UpdateProfileRequest request);

  /**
   * Gets bank account information for a user.
   *
   * @param userId the user ID
   * @return the bank account information
   */
  BankAccountDto getBankAccountInfo(UUID userId);

  void deleteClientById(UUID id);

  /**
   * Enables a client account (admin operation).
   *
   * @param userId the user ID
   * @return the updated user
   */
  User enableClient(UUID userId);

  /**
   * Disables a client account (admin operation).
   *
   * @param userId the user ID
   * @return the updated user
   */
  User disableClient(UUID userId);
}
