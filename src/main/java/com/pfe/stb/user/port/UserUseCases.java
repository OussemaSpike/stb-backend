package com.pfe.stb.user.port;

import com.pfe.stb.user.model.User;
import java.util.UUID;

/** Interface defining the use cases for managing users. */
public interface UserUseCases {

  /**
   * Finds a user by their unique identifier.
   *
   * @param id the unique identifier of the user
   * @return the user associated with the given id
   */
  User findById(UUID id);
}
