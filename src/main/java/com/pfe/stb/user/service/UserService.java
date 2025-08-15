package com.pfe.stb.user.service;

import com.pfe.stb.exception.NotFoundException;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.port.UserUseCases;
import com.pfe.stb.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCases {

  private final UserRepository userRepository;

  @Override
  public User findById(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }
}
