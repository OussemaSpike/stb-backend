package com.pfe.stb.user.controller;

import com.pfe.stb.shared.pagination.CustomPage;
import com.pfe.stb.shared.pagination.PageMapper;
import com.pfe.stb.shared.pagination.PaginationUtils;
import com.pfe.stb.shared.security.AuthUtils;
import com.pfe.stb.user.dto.request.CreateClientRequest;
import com.pfe.stb.user.dto.request.UpdateProfileRequest;
import com.pfe.stb.user.dto.response.BankAccountDto;
import com.pfe.stb.user.dto.response.UserDto;
import com.pfe.stb.user.dto.response.UserForAdminDto;
import com.pfe.stb.user.mapper.UserMapper;
import com.pfe.stb.user.model.User;
import com.pfe.stb.user.model.enums.RoleType;
import com.pfe.stb.user.port.UserUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

  @Operation(
      summary = "Find user by ID for admin View",
      description = "Retrieve user information by their unique ID.")
  @GetMapping("/details/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserForAdminDto> findByIdForAdmin(@PathVariable UUID id) {
    return ResponseEntity.ok(userMapper.toUserForAdminDto(usersUseCases.findById(id)));
  }

  @Operation(
      summary = "Find user by ID",
      description = "Retrieve user information by their unique ID.")
  @GetMapping("/me")
  public ResponseEntity<UserDto> findCurrentUser() {
    UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
    return ResponseEntity.ok(userMapper.toUserDto(usersUseCases.findById(userId)));
  }

  @Operation(
      summary = "Find all users",
      description =
          "Retrieve all users with pagination, sorting, and filtering options. Supports sorting by fields and filtering based on criteria.")
  @GetMapping()
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CustomPage<UserDto>> findAll(
      @RequestParam(defaultValue = "0", required = false) int page,
      @RequestParam(defaultValue = "10", required = false) int size,
      @RequestParam(defaultValue = "id", required = false) String sort,
      @RequestParam(defaultValue = "", required = false) String search,
      @RequestParam(defaultValue = "DESC") String sortDirection,
      @RequestParam(required = false) RoleType role) {
    Pageable pageable = PaginationUtils.createPageable(page, size, sort, sortDirection);

    Page<UserDto> usersPage =
        usersUseCases.findAll(search, role, pageable).map(userMapper::toUserDto);
    return ResponseEntity.ok(PageMapper.toCustomPage(usersPage));
  }

  @Operation(
      summary = "Create a new client user (admin only)",
      description = "Admin creates a new client account with verified information.")
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDto> createClient(@RequestBody CreateClientRequest request) {
    User user = usersUseCases.createClient(request);
    return ResponseEntity.ok(userMapper.toUserDto(user));
  }

  @Operation(
      summary = "Delete a client by Id",
      description = "Admin can delete a client user by their unique ID.")
  @DeleteMapping("{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
    usersUseCases.deleteClientById(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Update current user's profile",
      description = "Client can update their first name, last name, phone number, and address.")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasRole('CLIENT')")
  @PutMapping("/profile")
  public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
    UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
    User updatedUser = usersUseCases.updateProfile(userId, request);
    return ResponseEntity.ok(userMapper.toUserDto(updatedUser));
  }

  @Operation(
      summary = "Get current user's bank account information",
      description = "Client can view their bank account details including balance.")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasRole('CLIENT')")
  @GetMapping("/profile/bank-account")
  public ResponseEntity<BankAccountDto> getBankAccount() {
    UUID userId = AuthUtils.getCurrentAuthenticatedUserId();
    BankAccountDto bankAccount = usersUseCases.getBankAccountInfo(userId);
    return ResponseEntity.ok(bankAccount);
  }

  @Operation(
      summary = "Enable a client account (admin only)",
      description = "Admin can enable a disabled client account to allow them to use the system.")
  @PostMapping("/{id}/enable")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDto> enableClient(@PathVariable UUID id) {
    User user = usersUseCases.enableClient(id);
    return ResponseEntity.ok(userMapper.toUserDto(user));
  }

  @Operation(
      summary = "Disable a client account (admin only)",
      description = "Admin can disable a client account to prevent them from using the system.")
  @PostMapping("/{id}/disable")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDto> disableClient(@PathVariable UUID id) {
    User user = usersUseCases.disableClient(id);
    return ResponseEntity.ok(userMapper.toUserDto(user));
  }
}
