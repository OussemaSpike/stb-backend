package com.pfe.stb.security;

import com.pfe.stb.exception.NotFoundException;
import com.pfe.stb.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsServiceImpl is a service that implements the UserDetailsService interface. It is used
 * to load user-specific data during the authentication process.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Loads the user's details by their email. This method is called by Spring Security during the
   * authentication process.
   * --------------------------------------------------------------------------- The @Transactional
   * annotation ensures that the method runs within a transaction. If an exception occurs, the
   * transaction will be rolled back.
   *
   * @param username the email of the user
   * @return the UserDetails of the user
   * @throws UsernameNotFoundException if the user is not found
   */
  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(username)
        .orElseThrow(
            () ->
                new NotFoundException(
                    NotFoundException.NotFoundExceptionType.USER_NOT_FOUND, username));
  }
}
