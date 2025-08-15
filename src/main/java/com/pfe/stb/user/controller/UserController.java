package com.pfe.stb.user.controller;

import com.pfe.stb.user.dto.response.UserDto;
import com.pfe.stb.user.mapper.UserMapper;
import com.pfe.stb.user.port.UserUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Operations related to managing users")
@RequiredArgsConstructor
public class UserController {
  private final UserUseCases usersUseCases;
  private final UserMapper userMapper;

  @Operation(
      summary = "Find user by ID",
      description = "Retrieve user information by their unique ID.")
  @GetMapping("{id}")
  public ResponseEntity<UserDto> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(userMapper.toUserDto(usersUseCases.findById(id)));
  }
}
